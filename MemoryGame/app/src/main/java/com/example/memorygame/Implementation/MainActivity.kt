package com.example.memorygame.Implementation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memorygame.ui.theme.MemoryGameTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memorygame.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.memorygame.R

/**
 * Activitat principal que gestiona el joc de Memory.
 * Aquesta activitat és responsable de coordinar les diferents pantalles del joc, com ara la pantalla d'inici, la configuració,
 * la pantalla de joc i la pantalla de finalització de la partida.
 * @property playerName Nom del jugador actual.
 * @property memoryViewModel ViewModel que gestiona la lògica de negoci del joc.
 * @property globalDifficulty Dificultat global del joc.
 * @property email Adreça d'email introduïda pel jugador per enviar els detalls de la partida.
 * @property musicActive Indica si la música de fons està activada o no.
 * @constructor Crea una nova instància de la classe MainActivity.
 */
class MainActivity : ComponentActivity() {
    /**
     * Nom del jugador actual.
     */
    private var playerName by mutableStateOf("")

    /**
     * ViewModel que gestiona la lògica de negoci del joc de Memory.
     */
    private val memoryViewModel: MemoryViewModel by viewModels()

    /**
     * Dificultat global del joc.
     */
    private var globalDifficulty by mutableStateOf("")

    /**
     * Adreça d'email introduïda pel jugador per enviar els detalls de la partida.
     */
    private var email = ""

    /**
     * Indica si la música de fons està activada o no.
     */
    private var musicActive = false

    /**
     * Mètode que s'executa en crear l'activitat.
     * Configura el contingut de l'activitat, que inclou la navegació entre les diferents pantalles del joc.
     * @param savedInstanceState Estat anterior de l'activitat, si n'hi ha.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MemoryGameTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "start") {
                    // Pantalla inicial
                    composable("start") {
                        StartScreen(
                            navigateToConfig = { navController.navigate("configuracio") }, // Cambiado
                            navigateToInfo = { navController.navigate("info") },
                            navigateToExit = { navController.navigate("exit") }
                        )
                    }

                    // Pantalla de configuración
                    composable("configuracio") {
                        LaunchLoadGame()
                        ConfigurationScreen { useTimer ->
                            navController.navigate("loadGame/$useTimer")
                        }
                    }

                    // Pantalla del joc
                    composable("loadGame/{useTimer}") { backStackEntry ->
                        val useTimer =
                            backStackEntry.arguments?.getString("useTimer")?.toBoolean() ?: false
                        LoadGame(navController, memoryViewModel, useTimer)
                        if (!musicActive) {
                            startService(Intent(this@MainActivity, MusicService::class.java))
                            musicActive = true
                        }
                    }

                    // Final partida
                    composable("gameFinished") {
                        if (musicActive) {
                            stopService(Intent(this@MainActivity, MusicService::class.java))
                        }
                        GameFinishedScreen(
                            cardsMatched = memoryViewModel.getCardsMatchedCount(),
                            cardsNotMatched = memoryViewModel.getCardsNotMatchedCount(),
                            email = email,
                            onEmailChanged = { email = it },
                            navigateToConfig = { navController.navigate("configuracio") },
                            navigateToExit = { finish() } // Finalizar la aplicación
                        )
                    }

                    // Pantalla d'información
                    composable("info") {
                        PantallaInfo(onBackPressed = { navController.popBackStack() })
                    }

                    // Finalitzar l'aplicació
                    composable("exit") {
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Mètode utilitzat per carregar la partida abans de mostrar la pantalla de joc.
     * Utilitza un LaunchedEffect per carregar les cartes i barrejar-les.
     */
    @Composable
    private fun LaunchLoadGame() {
        LaunchedEffect(Unit) {
            while (globalDifficulty.isEmpty()) {
                delay(100)
            }
            memoryViewModel.uploadDifficulty(globalDifficulty)
            memoryViewModel.loadCardsAndShuffle()
        }
    }

    /**
     * Mètode que s'executa en destruir l'activitat.
     * S'encarrega d'aturar el servei de música de fons.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this@MainActivity, MusicService::class.java))
    }

    /**
     * Funció Composable que mostra la primera pantalla de l'aplicació, que inclou els botons per començar la partida,
     * accedir a la pantalla d'informació o sortir del joc.
     *
     * @param navigateToConfig Funció de callback per navegar a la pantalla de configuració.
     * @param navigateToInfo Funció de callback per navegar a la pantalla d'informació.
     * @param navigateToExit Funció de callback per sortir del joc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun StartScreen(
        navigateToConfig: () -> Unit,
        navigateToInfo: () -> Unit,
        navigateToExit: () -> Unit
    ) {
        Surface(
            color = BlueLight,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),

                ) {
                Text(
                    text = "Memory Game",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Botó per començar el joc: Start Game -> Configuració -> LoadGame
                Button(
                    onClick = { navigateToConfig() },
                    modifier = Modifier.width(150.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(BlueGreen, contentColor = Color.White),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = "Start Game")
                }

                // Botó per accedir a la pantalla d'informació
                Spacer(modifier = Modifier.height(16.dp)) // Espai entre els botons
                Button(
                    onClick = { navigateToInfo() }, modifier = Modifier.width(150.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(BlueGreen, contentColor = Color.White),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = "Informació")
                }

                // Botó per sortir del joc
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navigateToExit() }, modifier = Modifier.width(150.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(BlueGreen, contentColor = Color.White),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = "Sortir del joc")
                }
            }
        }
    }

    /**
     * Funció Composable que mostra la pantalla d'informació del joc de Memory.
     * Aquesta pantalla inclou una descripció del joc, una imatge il·lustrativa i un botó per tornar enrere.
     *
     * @param onBackPressed Funció de callback que s'executarà quan es premi el botó per tornar enrere.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PantallaInfo(onBackPressed: () -> Unit) {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.info))
                },
            )
        }) {
            Surface(
                color = BlueLight,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 80.dp)
                ) {
                    Text(
                        text = "Benvingut al joc de MEMORY",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color.Black)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.description),
                            style = TextStyle(fontSize = 18.sp),
                            textAlign = TextAlign.Justify
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.memory_info),
                        contentDescription = "Foto info",
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onBackPressed,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(BlueGreen, contentColor = Color.White),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(text = "Tornar enrere")
                    }
                }
            }
        }
    }

    /**
     * Funció composable que representa la pantalla de configuració del joc Memory.
     *
     * Aquesta pantalla permet als jugadors configurar diversos paràmetres abans de començar la partida.
     * Els jugadors poden introduir el seu nom, seleccionar la dificultat del joc i activar o desactivar el temporitzador.
     * Un cop s'ha seleccionat la dificultat i s'ha introduït el nom del jugador, es pot iniciar la partida.
     *
     * @param navigateToLoadGame La funció per a la navegació cap a la pantalla de càrrega de la partida amb els paràmetres configurats.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConfigurationScreen(navigateToLoadGame: (useTimer: Boolean) -> Unit) {
        var selectedDifficulty by remember { mutableStateOf("") }
        var useTimer by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.config))
                    },
                )
            },
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    color = BlueLight,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Nom jugador
                        TextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Nom del Jugador") },
                            modifier = Modifier.padding(16.dp)
                        )

                        memoryViewModel.playerName = playerName

                        // Input per al nom del jugador
                        Text(text = "Selecciona la dificultad:")
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            DificultyButton("Fàcil", selectedDifficulty == "Fàcil") {
                                selectedDifficulty = "Fàcil"
                            }
                            DificultyButton(
                                "Intermedia",
                                selectedDifficulty == "Intermedia"
                            ) { selectedDifficulty = "Intermedia" }
                            DificultyButton("Difícil", selectedDifficulty == "Difícil") {
                                selectedDifficulty = "Difícil"
                            }
                        }

                        // Checkbox per activar o desactivar el temporitzador
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = useTimer,
                                onCheckedChange = { useTimer = it }
                            )
                            Text(text = "Activar temporitzador")
                        }

                        // Botó per iniciar la partida
                        Button(
                            onClick = {
                                globalDifficulty = selectedDifficulty
                                navigateToLoadGame(useTimer)
                            },
                            enabled = selectedDifficulty.isNotEmpty() && playerName.isNotEmpty(),
                            modifier = Modifier.width(150.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                BlueGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(text = "Iniciar Partida")
                        }
                    }
                }
            }
        )
    }

    /**
     * Funció Composable que mostra un botó per seleccionar la dificultat del joc de Memory.
     * Aquest botó permet als usuaris especificar la dificultat del joc (fàcil, intermedi o difícil).
     *
     * @param text Text que s'ha de mostrar dins del botó per indicar la dificultat.
     * @param isSelected Booleà que indica si el botó està seleccionat o no.
     * @param onSelectDifficulty Funció de callback que s'executarà quan es seleccioni la dificultat.
     */
    @Composable
    fun DificultyButton(text: String, isSelected: Boolean, onSelectDifficulty: () -> Unit) {
        // Determinar els colors del botó basats en si està seleccionat o no
        val backgroundColor = if (isSelected) BlueGreen else BlueGreenLight
        val contentColor = if (isSelected) Color.White else Color.Black

        // Mostrar el botó amb la dificultat especificada
        Button(
            onClick = onSelectDifficulty,
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(backgroundColor, contentColor = contentColor),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
        ) {
            Text(text = text)
        }
    }

    /**
     * Funció Composable que representa la pantalla de càrrega i visualització del joc de Memory.
     * Aquesta pantalla mostra les cartes del joc i controla el temps restant de la partida si s'activa el temporitzador.
     * També controla si la partida ha finalitzat i navega a la pantalla de finalització
     *
     * @param navController Controlador de navegació per a la navegació entre pantalles.
     * @param memoryViewModel ViewModel que conté la lògica del joc de Memory.
     * @param useTimer Indica si s'ha d'activar el temporitzador per a la partida.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoadGame(
        navController: NavController,
        memoryViewModel: MemoryViewModel,
        useTimer: Boolean
    ) {
        // Estat del temps restant de la partida
        val remainingTime = remember { mutableLongStateOf(60L) }

        // Indica si la partida ha finalitzat
        val gameEnded =
            remember { mutableStateOf(false) }

        // Iniciar el temporitzador si s'activa i la partida no ha acabat
        if (useTimer && !gameEnded.value) {
            LaunchedEffect(key1 = true) {
                while (remainingTime.longValue > 0) {
                    delay(1000)
                    remainingTime.longValue -= 1
                }
                gameEnded.value = true
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(id = R.string.app_name))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = playerName)
                        }
                    },
                    actions = {
                        if (useTimer) Text(text = "Time: ${remainingTime.longValue} sec")
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp)
            ) {
                // Mostrar cartes y graella
                val cards: List<Cards> by memoryViewModel.getCards().observeAsState(listOf())
                val cardImages: List<Painter> =
                    memoryViewModel.obtainCardImages().map { painterResource(it) }
                CardsGrid(cards, cardImages)

                // Navegar a la pantalla de final de partida si la partida ha acabat
                if (gameEnded.value || memoryViewModel.allCardsMatched()) {
                    navController.navigate("gameFinished")
                }

            }
        }
    }

    /**
     * Funció Composable que mostra les cartes del joc de Memory en una graella.
     *
     * @param cards Llista de les cartes del joc.
     * @param cardImages Llista de les imatges de les cartes.
     */
    @Composable
    fun CardsGrid(cards: List<Cards>, cardImages: List<Painter>) {
        // Resolució del nombre de columnes de la graella basat en la dificultat global del joc
        val columns = when (globalDifficulty) {
            "Fàcil" -> 3    // 3 * 3
            "Intermedia" -> 4   // 4 * 4
            "Difícil" -> 4  // 4 * 6
            else -> 4
        }

        // Graella LazyVerticalGrid per mostrar les cartes
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            columns = GridCells.Fixed(columns)
        ) {
            items(cards.size) { cardIndex ->
                CardItem(cards[cardIndex], cardImages[cardIndex])
            }
        }
    }

    /**
     * Composable que representa una carta en la graella del joc.
     *
     * Aquesta funció crea una carta visual que es pot girar al fer-hi clic. La rotació de la carta s'anima
     * per proporcionar una experiència visual agradable a l'usuari.
     *
     * @param card La carta que es representarà.
     * @param cardImage La imatge de la carta.
     */

    @Composable
    fun CardItem(card: Cards, cardImage: Painter) {
        // Estat de rotació de la carta
        var rotationState by remember { mutableFloatStateOf(0f) }

        // Angle de rotació animat
        val rotationAngle by animateFloatAsState(
            targetValue = rotationState,
            animationSpec = TweenSpec(durationMillis = 500, easing = FastOutSlowInEasing),
            label = ""
        )

        Box(
            modifier = Modifier.padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.Black.copy(alpha = if (card.isVisible) 0.4F else 0.0F),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        if (card.isVisible) {
                            rotationState += 180f
                            memoryViewModel.updateVisibleCardStates(card.id)
                        }
                    }
                    .graphicsLayer(
                        rotationY = rotationAngle
                    )
            ) {
                Image(
                    painter = if (card.isVisible && !card.isSelected) painterResource(R.drawable.carta_revers) else cardImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }

    /**
     * Funció Composable que representa la pantalla de finalització del joc de Memory.
     *
     * Aquesta pantalla mostra els resultats finals de la partida de Memory, incloent el nombre
     * de cartes encertades i no encertades, la data i hora de finalització de la partida, i proporciona
     * opcions per enviar aquests resultats per correu electrònic, tornar a jugar o sortir del joc.
     *
     * @param cardsMatched El nombre de cartes encertades durant la partida.
     * @param cardsNotMatched El nombre de cartes no encertades durant la partida.
     * @param email L'adreça de correu electrònic del destinatari per enviar els resultats del joc.
     * @param onEmailChanged La funció que s'executa quan l'adreça de correu electrònic canvia.
     * @param navigateToConfig La funció per navegar a la pantalla de configuració del joc.
     * @param navigateToExit La funció per sortir del joc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GameFinishedScreen(
        cardsMatched: Int,
        cardsNotMatched: Int,
        email: String,
        onEmailChanged: (String) -> Unit,
        navigateToConfig: () -> Unit,
        navigateToExit: () -> Unit
    ) {
        val currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.pantallaFi))
                    },
                    Modifier.background(BlueLight)
                )
            },
            modifier = Modifier.fillMaxSize(),
            content = {
                Surface(
                    color = BlueLight,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "¡Fi de la partida!",
                            style = TextStyle(fontSize = 24.sp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                        Text(
                            text = "Data i hora: ${currentDateTime.format(formatter)}"
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Mostrar valors del registre de la partida
                        Text(
                            text = "Valors del registre de la partida:",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Nom del jugador: ")
                                }
                                append("$playerName, ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Cartes encertades: ")
                                }
                                append("$cardsMatched, ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Cartes no encertades: ")
                                }
                                append("$cardsNotMatched")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Email
                        Text(
                            text = "Email del destinatari",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = email,
                            onValueChange = { onEmailChanged(it) },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val emailContent = buildString {
                                    append("Data i hora: $currentDateTime\n")
                                    append("Nom jugador: $playerName\n")
                                    append("Cartes encertades: $cardsMatched\n")
                                    append("Cartes no encertades: $cardsNotMatched\n")
                                }
                                sendEmail(context, emailContent)
                            },
                            modifier = Modifier.width(150.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                BlueGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(text = "Enviar Email")
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tornar a jugar (no funciona, al tornar a la configuració i voler jugar la partida finalitza i vas a la pantalla de fi partida)
                        Button(
                            onClick = {
                                memoryViewModel.restartGame()
                                navigateToConfig()
                            },
                            modifier = Modifier.width(150.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                BlueGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(text = "Tornar a jugar")
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Finalitzar l'aplicació
                        Button(
                            onClick = navigateToExit,
                            modifier = Modifier.width(150.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                BlueGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(text = "Sortir del joc")
                        }
                    }
                }
            }
        )
    }

    /**
     * Envia un correu electrònic amb els detalls de la partida.
     *
     * Aquesta funció crea un intent per enviar un correu electrònic utilitzant l'aplicació de correu electrònic
     * predeterminada del dispositiu. El contingut del correu electrònic inclou els detalls de la partida, com ara
     * la data i hora de la partida, el nom del jugador, el nombre de cartes encertades i no encertades.
     *
     * @param context El context de l'aplicació.
     * @param emailContent El contingut del correu electrònic que s'enviarà.
     */
    private fun sendEmail(context: Context, emailContent: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Detalls de la partida"
            )
            putExtra(Intent.EXTRA_TEXT, emailContent)
        }

        context.startActivity(intent)
    }
}
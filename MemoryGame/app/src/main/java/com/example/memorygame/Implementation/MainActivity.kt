package com.example.memorygame.Implementation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.lifecycleScope
import com.example.memorygame.DataBase.AppDatabase
import com.example.memorygame.DataBase.Partida
import com.example.memorygame.DataBase.PartidaDao
import com.example.memorygame.R
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 * Activitat principal que gestiona el joc de Memory.
 * Aquesta activitat és responsable de coordinar les diferents pantalles del joc, com ara la pantalla d'inici, la configuració,
 * la pantalla de joc i la pantalla de finalització de la partida.
 * @property playerName Nom del jugador actual.
 * @property memoryViewModel ViewModel que gestiona la lògica de negoci del joc.
 * @property globalDifficulty Dificultat global del joc.
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
    private var globalDifficulty = ""

    /**
     * Indica si la música de fons està activada o no.
     */
    private var musicActive = false

    /**
     * Indica si la partida ha estat guardada a la base de dades.
     * Quan és `true`, significa que la partida ha estat guardada.
     * Per defecte, la seva valor és `false`.
     */
    var saved: Boolean = false

    /**
     * Indica si el temporitzador està en marxa.
     * Quan és `true`, significa que el temporitzador està actiu.
     * Per defecte, la seva valor és `false`.
     */
    var timer: Boolean = false

    /**
     * Indica la classe de grandària de la finestra, que pot ser `WindowSizeClass.Portrait` o `WindowSizeClass.Landscape`.
     * Aquesta propietat s'inicialitza més endavant una vegada que la finestra s'ha carregat completament.
     */
    lateinit var sizeClass: WindowSizeClass

    /**
     * Indica si s'ha de mostrar només un panell.
     * Quan és `true`, significa que només s'ha de mostrar un panell.
     * Aquesta propietat s'assigna més endavant, depenent de la grandària de la finestra i d'altres factors.
     */
    var showOnePanel by Delegates.notNull<Boolean>()

    /**
     * Mètode que s'executa en crear l'activitat.
     * Configura el contingut de l'activitat, que inclou la navegació entre les diferents pantalles del joc.
     * @param savedInstanceState Estat anterior de l'activitat, si n'hi ha.
     */
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MemoryGameTheme {
                sizeClass = calculateWindowSizeClass(activity = this)
                showOnePanel = sizeClass.widthSizeClass == WindowWidthSizeClass.Compact
                val gameFinished by memoryViewModel.gameFinished.observeAsState(false)
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "start") {
                    // Pantalla inicial
                    composable("start") {
                        StartScreen(
                            navigateToConfig = { navController.navigate("configuracio") },
                            navigateToInfo = { navController.navigate("info") },
                            navigateToExit = { navController.navigate("exit") },
                            navigateToGame = { navController.navigate("loadGame/") },
                            navigateToSavedGames = { navController.navigate("savedGames") }
                        )
                    }

                    // Pantalla de configuración (Botó)
                    composable("configuracio") {
                        ConfigurationScreen(navController)
                    }

                    // Pantalla del joc
                    composable("loadGame/") { backStackEntry ->
                        LaunchLoadGame()    //-
                        LoadGame(navController, memoryViewModel, gameFinished)
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
                            navigateToGame = { navController.popBackStack("loadGame/", false) },
                            navigateToExit = { finish() },
                            navigateToConfig = {
                                navController.navigate(
                                    "configuracio"
                                )
                            },
                            navigateToSavedGames = { navController.navigate("savedGames") }
                        )
                    }

                    // Saved Games
                    composable("savedGames") {
                        // Smartphone
                        if (showOnePanel) {
                            SavedGamesScreenPhone(
                                navigateBack = { navController.popBackStack() }
                            )
                        } else {
                            SavedGamesScreenTablet(
                                navigateBack = { navController.popBackStack() }
                            )
                        }
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
            if (globalDifficulty.isEmpty()) {
                globalDifficulty = "Intermedia"
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
     * Mètode per desar l'estat actual de l'activitat en un objecte Bundle.
     * Això és especialment útil quan es necessita desar l'estat de l'aplicació per a ser restaurat posteriorment,
     * com ara els valors de les variables i dades relacionades amb l'estat del joc.
     *
     * @param outState L'objecte Bundle en el qual desar l'estat de l'activitat.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        memoryViewModel.gameFinished.value?.let { outState.putBoolean("gameFinished", it) }
    }

    /**
     * Mèètode per restaurar l'estat de l'activitat després d'haver estat reinicialitzada,
     * recuperant les dades desades anteriorment a través del mètode `onSaveInstanceState`.
     *
     * @param savedInstanceState L'objecte Bundle que conté l'estat de l'activitat desada.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val gameFinished = savedInstanceState.getBoolean("gameFinished")
    }


    /**
     * Funció Composable que mostra la primera pantalla de l'aplicació, que inclou els botons per començar la partida,
     * accedir a la pantalla d'informació o sortir del joc.
     *
     * @param navigateToConfig Funció de callback per navegar a la pantalla de configuració.
     * @param navigateToInfo Funció de callback per navegar a la pantalla d'informació.
     * @param navigateToExit Funció de callback per sortir del joc.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun StartScreen(
        navigateToConfig: () -> Unit,
        navigateToInfo: () -> Unit,
        navigateToExit: () -> Unit,
        navigateToGame: () -> Unit,
        navigateToSavedGames: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(BlueLight),
                    title = { Box(modifier = Modifier.fillMaxWidth()) },
                    actions = {
                        IconButton(
                            onClick = { navigateToConfig() },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuració")
                        }
                    }
                )
            }
        ) {
            Surface(
                color = BlueLight,
                modifier = Modifier.fillMaxSize(),
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
                            fontWeight = FontWeight.Bold,
                            color = BlueGreenDark
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    val buttonModifier = Modifier
                        .width(200.dp)
                        .height(60.dp)
                        .padding(vertical = 8.dp)

                    OutlinedButtonWithShakeAnimation(
                        onClick = { navigateToGame() },
                        modifier = buttonModifier,
                        text = "Iniciar partida",
                        animate = true
                    )

                    OutlinedButtonWithShakeAnimation(
                        onClick = { navigateToInfo() },
                        modifier = buttonModifier,
                        text = "Informació"
                    )

                    OutlinedButtonWithShakeAnimation(
                        onClick = { navigateToSavedGames() },
                        modifier = buttonModifier,
                        text = "Historial"
                    )

                    OutlinedButtonWithShakeAnimation(
                        onClick = { navigateToExit() },
                        modifier = buttonModifier,
                        text = "Sortir del joc"
                    )
                }
            }
        }
    }

    /**
     * Una funció @Composable que crea un botó amb contorn i efecte d'animació de tremolor.
     *
     * @param onClick Callback que s'invocarà quan es faci clic al botó.
     * @param modifier El modificiador a aplicar al botó.
     * @param text El text que es mostrarà al botó.
     * @param animate Un paràmetre booleà per habilitar o deshabilitar l'animació. Per defecte és false.
     */
    @Composable
    fun OutlinedButtonWithShakeAnimation(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        text: String,
        animate: Boolean = false
    ) {
        val shakeDuration = 1000
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val shakeAnimation by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = shakeDuration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val modifierWithAnimation = if (animate) {
            modifier.then(Modifier.offset(x = shakeAnimation.dp))
        } else {
            modifier
        }

        OutlinedButton(
            onClick = onClick,
            modifier = modifierWithAnimation,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, BlueGreenDark),
            colors = ButtonDefaults.outlinedButtonColors(
                BlueGreenDark,
                contentColor = BlueLight
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                color = BlueLight
            )
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.info))
                    },
                )
            }
        ) {
            Surface(
                color = BlueLight,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(
                        top = 0.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    Text(
                        text = "Benvingut al joc de MEMORY",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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
                        modifier = Modifier
                            .size(250.dp)
                            .padding(top = 16.dp, bottom = 10.dp)
                    )

                    Button(
                        onClick = onBackPressed,
                        modifier = Modifier.padding(horizontal = 16.dp),
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
    fun ConfigurationScreen(navController: NavController) {
        // Estats dels camps de configuració
        var selectedDifficulty by remember { mutableStateOf("") }
        var useTimer by remember { mutableStateOf(false) }
        var name: String by remember { mutableStateOf("") }

        // Scaffold amb TopAppBar i contingut
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
                ConfigurationContent(
                    selectedDifficulty = selectedDifficulty,
                    useTimer = useTimer,
                    name = name,
                    onNameChange = { newName -> name = newName },
                    onDifficultySelected = { newDifficulty -> selectedDifficulty = newDifficulty },
                    onTimerCheckboxChanged = { newTimerValue -> useTimer = newTimerValue },
                    onBackButtonClicked = {
                        globalDifficulty = selectedDifficulty
                        timer = useTimer
                        navController.popBackStack()
                    }
                )
            }
        )
    }

    /**
     * Funció que representa el contingut de la pantalla de configuració.
     *
     * @param selectedDifficulty Dificultat seleccionada.
     * @param useTimer Indica si s'activa el temporitzador.
     * @param name Nom del jugador.
     * @param onNameChange Funció de callback per canviar el nom del jugador.
     * @param onDifficultySelected Funció de callback per seleccionar la dificultat.
     * @param onTimerCheckboxChanged Funció de callback per canviar l'estat del temporitzador.
     * @param onBackButtonClicked Funció de callback per gestionar l'acció de tornar enrere.
     */
    @Composable
    private fun ConfigurationContent(
        selectedDifficulty: String,
        useTimer: Boolean,
        name: String,
        onNameChange: (String) -> Unit,
        onDifficultySelected: (String) -> Unit,
        onTimerCheckboxChanged: (Boolean) -> Unit,
        onBackButtonClicked: () -> Unit
    ) {
        // Columna amb contingut de configuració
        Surface(
            color = BlueLight,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Camp de nom del jugador
                PlayerNameTextField(name = name, onNameChange = onNameChange)

                // Seleccionar dificultat
                DifficultySelection(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = onDifficultySelected
                )

                // Checkbox per activar o desactivar el temporitzador
                TimerCheckbox(useTimer = useTimer, onTimerCheckboxChanged = onTimerCheckboxChanged)

                // Botó per tornar enrere
                BackButton(onBackButtonClicked = onBackButtonClicked)
            }
        }
    }

    /**
     * Funció que representa el camp de text del nom del jugador.
     *
     * @param name Nom actual del jugador.
     * @param onNameChange Funció de callback per canviar el nom del jugador.
     */
    @Composable
    private fun PlayerNameTextField(name: String, onNameChange: (String) -> Unit) {
        Text(
            text = "Nom del jugador",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nom del jugador") },
            modifier = Modifier,
        )
        playerName = name
    }

    /**
     * Funció que representa la selecció de dificultat.
     *
     * @param selectedDifficulty Dificultat seleccionada.
     * @param onDifficultySelected Funció de callback per seleccionar la dificultat.
     */
    @Composable
    private fun DifficultySelection(
        selectedDifficulty: String,
        onDifficultySelected: (String) -> Unit
    ) {
        Text(text = "Seleccionar la dificultat:")
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            DificultyButton("Fàcil", selectedDifficulty == "Fàcil") {
                onDifficultySelected("Fàcil")
            }
            DificultyButton(
                "Intermedia",
                selectedDifficulty == "Intermedia"
            ) { onDifficultySelected("Intermedia") }
            DificultyButton("Difícil", selectedDifficulty == "Difícil") {
                onDifficultySelected("Difícil")
            }
        }
    }

    /**
     * Funció que representa la casella de selecció del temporitzador.
     *
     * @param useTimer Indica si s'activa el temporitzador.
     * @param onTimerCheckboxChanged Funció de callback per canviar l'estat del temporitzador.
     */
    @Composable
    private fun TimerCheckbox(useTimer: Boolean, onTimerCheckboxChanged: (Boolean) -> Unit) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Checkbox(
                checked = useTimer,
                onCheckedChange = onTimerCheckboxChanged
            )
            Text(text = "Activar temporitzador")
        }
    }

    /**
     * Funció que representa el botó per tornar enrere.
     *
     * @param onBackButtonClicked Funció de callback per gestionar l'acció de tornar enrere.
     */
    @Composable
    private fun BackButton(onBackButtonClicked: () -> Unit) {
        Button(
            onClick = onBackButtonClicked,
            modifier = Modifier.width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                BlueGreen,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
        ) {
            Text(text = "Tornar enrere")
        }
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

        val halfScreenWidth =
            with(LocalDensity.current) { (LocalConfiguration.current.screenWidthDp.dp / 3) }

        val halfSizeModifier = Modifier.width(halfScreenWidth)

        // Mostrar el botó amb la dificultat especificada
        Button(
            onClick = onSelectDifficulty,
            modifier = halfSizeModifier,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(backgroundColor, contentColor = contentColor),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
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
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun LoadGame(
        navController: NavController,
        memoryViewModel: MemoryViewModel,
        gameFinished: Boolean
    ) {
        val sizeClass = calculateWindowSizeClass(activity = this)
        val showOnePanel = sizeClass.widthSizeClass == WindowWidthSizeClass.Compact

        // Estat del temps restant de la partida
        val remainingTime = remember { mutableLongStateOf(60L) }

        // Indica si la partida ha finalitzat
        val gameEnded = remember { mutableStateOf(false) }

        // Iniciar el temporitzador si s'activa i la partida no ha acabat
        if (timer && !gameEnded.value) {
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
                        if (timer) Text(text = "Time: ${remainingTime.longValue} sec")
                    }
                )
            }
        ) {
            if (showOnePanel) {
                // Si es mostra només un panell (per mòbil)
                LoadGamePhone(
                    navController = navController,
                    memoryViewModel = memoryViewModel,
                    gameFinished = gameFinished,
                    gameEnded = gameEnded
                )
            } else {
                // Si es mostra un panell a l'esquerra i l'altre a la dreta (per tauleta)
                LoadGameTablet(
                    navController = navController,
                    gameFinished = gameFinished,
                    gameEnded = gameEnded
                )
            }
        }
    }

    /**
     * Composable que mostra la pantalla de carregament del joc.
     *
     * Aquesta pantalla mostra les cartes i la graella del joc en curs. També gestiona la transició a la pantalla de final de partida
     * quan la partida ha acabat.
     *
     * @param navController Controlador de navegació per a la navegació entre pantalles.
     * @param memoryViewModel ViewModel que conté la lògica del joc de memòria.
     * @param gameFinished Indica si la partida ha acabat per causes naturals del joc.
     * @param gameEnded Estat mutable que indica si la partida ha acabat.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun LoadGamePhone(
        navController: NavController,
        memoryViewModel: MemoryViewModel,
        gameFinished: Boolean,
        gameEnded: MutableState<Boolean>
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BlueLight)
                .padding(top = 60.dp)
        ) {
            // Mostrar cartes i graella
            val cards: List<Cards> by memoryViewModel.getCards().observeAsState(listOf())
            val cardImages: List<Painter> =
                memoryViewModel.obtainCardImages().map { painterResource(it) }
            CardsGrid(cards, cardImages)

            // Navegar a la pantalla de final de partida si la partida ha acabat
            if (gameEnded.value || gameFinished) {
                saveAndFinishGame(
                    gameEnded = gameEnded,
                    gameFinished = gameFinished,
                    navController = navController
                )
            }
        }
    }

    /**
     * Composable que mostra la pantalla de carregament del joc en una tauleta.
     *
     * Aquesta pantalla s'adapta a la disposició de la tauleta i mostra el contingut del joc, gestionant
     * la transició a la pantalla de final de partida quan la partida ha acabat.
     *
     * @param navController Controlador de navegació per a la navegació entre pantalles.
     * @param gameFinished Indica si la partida ha acabat per causes naturals del joc.
     * @param gameEnded Estat mutable que indica si la partida ha acabat.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun LoadGameTablet(
        navController: NavController,
        gameFinished: Boolean,
        gameEnded: MutableState<Boolean>
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            LoadGameTabletLandscape(navController, gameFinished, gameEnded)
        } else {
            LoadGameTabletPortrait(navController, gameFinished, gameEnded)
        }

    }

    /**
     * Composable que mostra la pantalla de carregament del joc en mode paisatge en una tauleta.
     *
     * Aquesta pantalla està dissenyada per a dispositius en mode paisatge i mostra la graella de cartes a l'esquerra
     * i els resultats de la partida a la dreta. També gestiona la transició a la pantalla de final de partida
     * quan la partida ha acabat.
     *
     * @param navController Controlador de navegació per a la navegació entre pantalles.
     * @param gameFinished Indica si la partida ha acabat per causes naturals del joc.
     * @param gameEnded Estat mutable que indica si la partida ha acabat.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun LoadGameTabletLandscape(
        navController: NavController,
        gameFinished: Boolean,
        gameEnded: MutableState<Boolean>
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Graella de cartes a l'esquerra
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(top = 60.dp)
                    .background(color = BlueLight)
            ) {
                val cards: List<Cards> by memoryViewModel.getCards().observeAsState(listOf())
                val cardImages: List<Painter> =
                    memoryViewModel.obtainCardImages().map { painterResource(it) }
                CardsGrid(cards, cardImages)
            }
            // Resultats a la dreta
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(top = 60.dp)
                    .background(color = BlueLight)
            ) {
                item {
                    Text("Logs de la Partida", style = MaterialTheme.typography.headlineSmall)
                }

                // Aquí pots iterar sobre els logs de la partida
                val logs: List<String> = memoryViewModel.gameLogs
                items(logs.size) { index ->
                    Text(text = logs[index])
                }
            }

            // Navegar a la pantalla de final de partida si la partida ha acabat
            if (gameEnded.value || gameFinished) {
                saveAndFinishGame(
                    gameEnded = gameEnded,
                    gameFinished = gameFinished,
                    navController = navController
                )
            }
        }
    }

    /**
     * Composable que mostra la pantalla de carregament del joc en mode retrat en una tauleta.
     *
     * Aquesta pantalla està dissenyada per a dispositius en mode retrat i mostra la graella de cartes a la part superior
     * i els resultats de la partida a la part inferior. També gestiona la transició a la pantalla de final de partida
     * quan la partida ha acabat.
     *
     * @param navController Controlador de navegació per a la navegació entre pantalles.
     * @param gameFinished Indica si la partida ha acabat per causes naturals del joc.
     * @param gameEnded Estat mutable que indica si la partida ha acabat.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun LoadGameTabletPortrait(
        navController: NavController,
        gameFinished: Boolean,
        gameEnded: MutableState<Boolean>
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Graella de cartes a dalt
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 60.dp)
                    .background(color = BlueLight)
            ) {
                val cards: List<Cards> by memoryViewModel.getCards().observeAsState(listOf())
                val cardImages: List<Painter> =
                    memoryViewModel.obtainCardImages().map { painterResource(it) }
                CardsGrid(cards, cardImages)
            }
            // Resultats a sota
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    //.padding(top = 60.dp)
                    .background(color = BlueLight)
            ) {
                item {
                    Text("Logs de la Partida", style = MaterialTheme.typography.headlineSmall)
                }

                // Aquí pots iterar sobre els logs de la partida
                val logs: List<String> = memoryViewModel.gameLogs
                items(logs.size) { index ->
                    Text(text = logs[index])
                }
            }

            // Navegar a la pantalla de final de partida si la partida ha acabat
            if (gameEnded.value || gameFinished) {
                saveAndFinishGame(
                    gameEnded = gameEnded,
                    gameFinished = gameFinished,
                    navController = navController
                )
            }
        }
    }


    /**
     * Composable que guarda la partida a la base de dades i finalitza el joc si alguna de les següents condicions es compleix:
     * - El joc ha acabat (`gameEnded` és cert).
     * - El joc ha finalitzat (`gameFinished` és cert).
     *
     * @param gameEnded L'estat mutable que indica si el joc ha acabat.
     * @param gameFinished Booleà que indica si el joc ha finalitzat.
     * @param navController El NavController que s'utilitza per navegar a la pantalla de finalització del joc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun saveAndFinishGame(
        gameEnded: MutableState<Boolean>,
        gameFinished: Boolean,
        navController: NavController
    ) {
        if (gameEnded.value || gameFinished) {
            // Guardar partida a la base de dades
            if (!saved) {
                SaveGameToDb()
                saved = true
            }
            navController.navigate("gameFinished")
        }
    }

    /**
     * Composable que guarda les dades de la partida actual a la base de dades.
     * Les dades incloses en la partida són el nom del jugador, la data i hora actual,
     * si s'ha utilitzat el temporitzador, la dificultat del joc, el nombre de cartes jugades,
     * el nombre de cartes encertades i el nombre de cartes no encertades.
     *
     * @see Partida
     * @see AppDatabase
     * @see PartidaDao
     *
     * @throws IllegalStateException Si no es pot obtenir una instància de la base de dades.
     * @throws Exception Si hi ha un error en l'execució de la tasca de guardar la partida a la base de dades.
     */
    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun SaveGameToDb(): Unit {
        val currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
        val context = LocalContext.current
        val cardsMatched = memoryViewModel.getCardsMatchedCount()
        val cardsNotMatched = memoryViewModel.getCardsNotMatchedCount()

        // Obtenir instancia de la base de dades
        val db = AppDatabase.getDatabase(context)
        val partidaDao = db.partidaDao()

        // Crear una nova partida amb les dades de la partida actual
        val partida = Partida(
            nomJugador = playerName,
            dataHora = currentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
            temporitzador = timer,
            dificultat = globalDifficulty,
            numeroCartes = cardsMatched + cardsNotMatched,
            cartesEncertades = cardsMatched,
            cartesNoEncertades = cardsNotMatched
        )

        // Insetar partida a la base de dades
        lifecycleScope.launch {
            // Verificar si la partida està a la base de dades
            val existingPartida = partidaDao.getPartidaById(partida.id)

            // Insertar partida si no existeix a la base de dades
            if (existingPartida == null) {
                partidaDao.insertIfNotExists(partida)
            }
        }
    }

    /**
     * Funció Composable que mostra les cartes del joc de Memory en una graella.
     *
     * @param cards Llista de les cartes del joc.
     * @param cardImages Llista de les imatges de les cartes.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun CardsGrid(cards: List<Cards>, cardImages: List<Painter>) {
        // Resolució del nombre de columnes de la graella basat en la dificultat global del joc
        val columns = when (globalDifficulty) {
            "Fàcil" -> 3    // 3 * 3
            "Intermedia" -> 4   // 4 * 4
            "Difícil" -> 4  // 4 * 6
            else -> 4
        }

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp

        val cardSize =
            (screenWidth / columns).coerceAtMost(100.dp)

        // Graella LazyVerticalGrid per mostrar les cartes
        LazyVerticalGrid(
            modifier = Modifier.size(cardSize * columns * 2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            columns = GridCells.Fixed(columns)
        ) {
            items(cards.size) { cardIndex ->
                val card = cards[cardIndex]
                CardItem(
                    card,
                    cardImages[cardIndex],
                    onContinueClicked = { memoryViewModel.updateVisibleCardStates(card) },
                    cardSize = cardSize
                )
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
    fun CardItem(card: Cards, cardImage: Painter, onContinueClicked: () -> Unit, cardSize: Dp) {
        var rotationState by remember { mutableFloatStateOf(0f) }

        val rotationAngle by animateFloatAsState(
            targetValue = rotationState,
            animationSpec = TweenSpec(durationMillis = 500, easing = FastOutSlowInEasing),
            label = ""
        )

        LaunchedEffect(card.isSelected) {
            rotationState = if (card.isSelected) 180f else 0f
        }

        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(cardSize)
        ) {
            Box(
                modifier = Modifier
                    .size(cardSize + 10.dp)
                    .clickable {
                        if (card.isVisible && !card.isMatched && !card.isSelected) {
                            onContinueClicked()
                        }
                    }
                    .graphicsLayer(
                        rotationY = rotationAngle
                    )
                    .background(
                        color = Color.Black.copy(alpha = if (card.isVisible && !card.isSelected && !card.isMatched) 0.4F else 0.0F),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Image(
                    painter = if (!card.isMatched && !card.isSelected) painterResource(R.drawable.carta_revers) else cardImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
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
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GameFinishedScreen(
        navigateToGame: () -> Unit,
        navigateToExit: () -> Unit,
        navigateToConfig: () -> Unit,
        navigateToSavedGames: () -> Unit
    ) {
        val currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
        var email by remember { mutableStateOf("") }
        val isEmailValid: Boolean = email.isNotBlank()

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(BlueLight),
                    title = {
                        Text(text = stringResource(id = R.string.pantallaFi))
                        Box(modifier = Modifier.fillMaxWidth())
                    },
                    actions = {
                        IconButton(
                            onClick = { navigateToConfig() },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuració")
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize(),
            content = {
                GameFinishedContent(
                    currentDateTime = currentDateTime,
                    navigateToGame = navigateToGame,
                    email = email,
                    onEmailChange = { newEmail -> email = newEmail },
                    isEmailValid = isEmailValid,
                    navigateToExit = navigateToExit,
                    navigateToSavedGames = navigateToSavedGames
                )
            }
        )
    }

    /**
     * Composable per a la pantalla de fi de partida.
     *
     * @param currentDateTime Data i hora actual de la finalització de la partida.
     * @param navigateToGame Funció per a navegar a la pantalla de joc.
     * @param email Correu electrònic del destinatari.
     * @param onEmailChange Funció per a actualitzar el correu electrònic.
     * @param isEmailValid Booleà que indica si el correu electrònic és vàlid.
     * @param navigateToExit Funció per a navegar fora de l'aplicació.
     * @param navigateToSavedGames Funció per a navegar a la pantalla d'historial de partides guardades.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun GameFinishedContent(
        currentDateTime: LocalDateTime,
        navigateToGame: () -> Unit,
        email: String,
        onEmailChange: (String) -> Unit,
        isEmailValid: Boolean,
        navigateToExit: () -> Unit,
        navigateToSavedGames: () -> Unit
    ) {
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
                GameResultsSection()

                // Email
                EmailSection(email, onEmailChange)

                // Botons
                ButtonsSection(
                    navigateToGame = navigateToGame,
                    navigateToExit = navigateToExit,
                    navigateToSavedGames = navigateToSavedGames,
                    email = email,
                    isEmailValid = isEmailValid,
                    currentDateTime = currentDateTime
                )
            }
        }
    }

    /**
     * Secció que mostra els resultats de la partida.
     */
    @Composable
    private fun GameResultsSection() {
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
                append(playerName)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Cartes encertades: ")
                }
                append("${memoryViewModel.getCardsMatchedCount()}")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Cartes no encertades: ")
                }
                append("${memoryViewModel.getCardsNotMatchedCount()}")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    /**
     * Secció per a la introducció del correu electrònic del destinatari.
     *
     * @param email Correu electrònic actual.
     * @param onEmailChange Funció per a gestionar els canvis en el correu electrònic.
     */
    @Composable
    private fun EmailSection(
        email: String,
        onEmailChange: (String) -> Unit,
    ) {
        Text(
            text = "Email del destinatari",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    /**
     * Secció que conté els botons per a les accions de la pantalla de fi de partida.
     *
     * @param navigateToGame Funció per a navegar a la pantalla de joc.
     * @param navigateToExit Funció per a navegar fora de l'aplicació.
     * @param navigateToSavedGames Funció per a navegar a la pantalla d'historial de partides guardades.
     * @param email Correu electrònic actual.
     * @param isEmailValid Booleà que indica si el correu electrònic és vàlid.
     * @param currentDateTime Data i hora actual.
     */
    @Composable
    private fun ButtonsSection(
        navigateToGame: () -> Unit,
        navigateToExit: () -> Unit,
        navigateToSavedGames: () -> Unit,
        email: String,
        isEmailValid: Boolean,
        currentDateTime: LocalDateTime
    ) {
        val context = LocalContext.current

        SendEmailButton(
            onClick = {
                val emailContent = buildString {
                    append("Data i hora: $currentDateTime\n")
                    append("Nom jugador: $playerName\n")
                    append("Cartes encertades: ${memoryViewModel.getCardsMatchedCount()}\n")
                    append("Cartes no encertades: ${memoryViewModel.getCardsNotMatchedCount()}\n")
                }
                sendEmail(context, emailContent, email)
            },
            isEmailValid = isEmailValid
        )

        Spacer(modifier = Modifier.height(16.dp))

        RestartGameButton(
            onClick = {
                memoryViewModel.restartGame()
                musicActive = false
                saved = false
                navigateToGame()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SavedGamesButton(
            onClick = { navigateToSavedGames() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExitGameButton(
            onClick = navigateToExit
        )
    }

    /**
     * Botó per a l'enviament del correu electrònic amb els resultats de la partida.
     *
     * @param onClick Funció per a gestionar la pressió del botó.
     * @param isEmailValid Booleà que indica si el correu electrònic és vàlid.
     */
    @Composable
    private fun SendEmailButton(
        onClick: () -> Unit,
        isEmailValid: Boolean
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                BlueGreen,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
            enabled = isEmailValid
        ) {
            Text(text = "Enviar Email")
        }
    }

    /**
     * Botó per a reiniciar la partida.
     *
     * @param onClick Funció per a gestionar la pressió del botó.
     */
    @Composable
    private fun RestartGameButton(
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
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
    }

    /**
     * Botó per a accedir a la pantalla d'historial de partides guardades.
     *
     * @param onClick Funció per a gestionar la pressió del botó.
     */
    @Composable
    private fun SavedGamesButton(
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.width(150.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                BlueGreen,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
        ) {
            Text(text = "Historial")
        }
    }

    /**
     * Botó per a sortir de l'aplicació.
     *
     * @param onClick Funció per a gestionar la pressió del botó.
     */
    @Composable
    private fun ExitGameButton(
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
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

    /**
     * Mostra la pantalla de les partides guardades dissenyada específicament per a smartphones.
     *
     * Aquesta pantalla mostra una llista de les partides guardades, permet eliminar-les
     * i veure els detalls de cada partida seleccionada.
     *
     * @param navigateBack Funció de callback per navegar cap enrere.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SavedGamesScreenPhone(
        navigateBack: () -> Unit
    ) {
        val context = LocalContext.current
        val db = AppDatabase.getDatabase(context)
        val partidaDao = db.partidaDao()
        val savedGames by partidaDao.getPartides().observeAsState(initial = emptyList())
        val coroutineScope = rememberCoroutineScope()
        var selectedPartida by remember { mutableStateOf<Partida?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Partides Guardades") },
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Botón de eliminación en la parte superior derecha
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    // Eliminar totes les partidas
                                    partidaDao.deleteAll()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar totes les partides"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = BlueLight // Color de fondo del TopAppBar
                    )
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlueLight) // Color de fondo para toda la pantalla
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Partides Guardades",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White, // Color del texto
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Mostrar las partidas guardadas
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedGames) { partida ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(BlueGreenLight, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .clickable { selectedPartida = partida }
                            ) {
                                Text(text = "Nom del jugador: ${partida.nomJugador}")
                                Text(text = "Data i hora: ${partida.dataHora}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar popup con los detalles de la partida seleccionada
                    selectedPartida?.let { partida ->
                        AlertDialog(
                            onDismissRequest = { selectedPartida = null },
                            title = { Text("Detalls de la Partida") },
                            text = {
                                Column {
                                    GameDetailsText(partida)
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { selectedPartida = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BlueGreen, // Color del botón de confirmación BlueGreen
                                        contentColor = Color.White
                                    ),
                                ) {
                                    Text("Tancar")
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    /**
     * Mostra la pantalla de les partides guardades dissenyada específicament per a tauletes.
     *
     * Aquesta pantalla ajusta el seu disseny segons l'orientació de la tauleta,
     * mostrant una llista de les partides guardades i permetent veure els detalls
     * de cada partida seleccionada.
     *
     * @param navigateBack Funció de callback per navegar cap enrere.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SavedGamesScreenTablet(
        navigateBack: () -> Unit
    ) {
        val context = LocalContext.current
        val db = AppDatabase.getDatabase(context)
        val partidaDao = db.partidaDao()
        val savedGames by partidaDao.getPartides().observeAsState(initial = emptyList())

        val configuration = context.resources.configuration
        val isLandscape =
            configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            HistorialLandscape(savedGames, navigateBack, partidaDao)
        } else {
            HistorialPortrait(savedGames, navigateBack, partidaDao)
        }
    }

    /**
     * Mostra la pantalla de les partides guardades en mode paisatge en tauletes.
     *
     * Aquesta pantalla ofereix una interfície optimitzada per a la visualització en tauletes en mode paisatge.
     * Mostra una llista de les partides guardades a l'esquerra i els detalls de la partida seleccionada a la dreta.
     * Proporciona opcions per eliminar totes les partides i navegar cap enrere.
     *
     * @param savedGames Llista de partides guardades a mostrar.
     * @param navigateBack Funció de callback per navegar cap enrere.
     * @param partidaDao DAO de la partida per a les operacions de base de dades.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HistorialLandscape(
        savedGames: List<Partida>,
        navigateBack: () -> Unit,
        partidaDao: PartidaDao
    ) {
        val selectedPartida = remember { mutableStateOf<Partida?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Partides Guardades") },
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = BlueLight
                    ),
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    partidaDao.deleteAll()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar totes les partides"
                            )
                        }
                    }
                )
            },
            content = {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Panell esquerre: llista de partides
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(BlueLight)
                    ) {
                        Text(
                            text = "Partides Guardades",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .background(BlueLight)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(BlueLight)

                        ) {
                            items(savedGames) { partida ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .background(
                                            BlueGreenLight,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                        .clickable { selectedPartida.value = partida }
                                ) {
                                    Text(text = "Nom del jugador: ${partida.nomJugador}")
                                    Text(text = "Data i hora: ${partida.dataHora}")
                                }
                            }
                        }
                    }

                    // Panell dret: detalls de la partida seleccionada
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .background(BlueLight)
                    ) {
                        selectedPartida.value?.let { partida ->
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Detalls de la Partida",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                GameDetailsText(partida)
                            }
                        }
                    }
                }
            }
        )
    }

    /**
     * Mostra la pantalla de les partides guardades en mode retrat en tauletes.
     *
     * Aquesta pantalla ofereix una interfície optimitzada per a la visualització en tauletes en mode retrat.
     * Mostra una llista de les partides guardades a la part superior i els detalls de la partida seleccionada
     * a la part inferior. Proporciona opcions per eliminar totes les partides i navegar cap enrere.
     *
     * @param savedGames Llista de partides guardades a mostrar.
     * @param navigateBack Funció de callback per navegar cap enrere.
     * @param partidaDao DAO de la partida per a les operacions de base de dades.
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun HistorialPortrait(
        savedGames: List<Partida>,
        navigateBack: () -> Unit,
        partidaDao: PartidaDao
    ) {
        val selectedPartida = remember { mutableStateOf<Partida?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Partides Guardades") },
                    navigationIcon = {
                        IconButton(onClick = navigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = BlueLight
                    ),
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    partidaDao.deleteAll()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar totes les partides"
                            )
                        }
                    }
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlueLight),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Partides Guardades",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Panel superior: llista de partides
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(savedGames) { partida ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(BlueGreenLight, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .clickable { selectedPartida.value = partida }
                            ) {
                                Text(text = "Nom del jugador: ${partida.nomJugador}")
                                Text(text = "Data i hora: ${partida.dataHora}")
                            }
                        }
                    }

                    // Panell inferior: detalls de la partida seleccionada
                    selectedPartida.value?.let { partida ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueLight)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = "Detalls de la Partida",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                GameDetailsText(partida)
                            }
                        }
                    }


                }
            }
        )
    }

    /**
     * Composable que mostra els detalls d'una partida.
     *
     * Aquest component genera una columna amb els detalls de la partida,
     * incloent el nom del jugador, la data i hora, el temporitzador,
     * la dificultat, el nombre de cartes, les cartes encertades i les cartes no encertades.
     *
     * @param partida La partida de la qual es volen mostrar els detalls.
     */
    @Composable
    fun GameDetailsText(partida: Partida) {
        Column {
            GameDetailRow("Nom del jugador", partida.nomJugador)
            GameDetailRow("Data i hora", partida.dataHora)
            GameDetailRow("Temporitzador", if (partida.temporitzador) "Activat" else "Desactivat")
            GameDetailRow("Dificultat", partida.dificultat)
            GameDetailRow("Número de cartes", (partida.numeroCartes ?: "N/A").toString())
            GameDetailRow("Cartes encertades", partida.cartesEncertades.toString())
            GameDetailRow("Cartes no encertades", partida.cartesNoEncertades.toString())
        }
    }

    /**
     * Composable que mostra una fila de detall d'una partida.
     *
     * Aquest component mostra una fila de detall de la partida,
     * que consisteix en una etiqueta i el valor corresponent.
     *
     * @param label Etiqueta de la dada.
     * @param value Valor de la dada.
     */
    @Composable
    fun GameDetailRow(label: String, value: String) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

    /**
     * Envia un correu electrònic amb els detalls de la partida.
     *
     * Aquesta funció crea un intent per enviar un correu electrònic utilitzant l'aplicació de correu electrònic
     * predeterminada del dispositiu. El contingut del correu electrònic inclou els detalls de la partida, com ara
     * la data i hora de la partida, el nom del jugador, el nombre de cartes encertades i no encertades.
     *
     * Després d'enviar el correu electrònic, la pantalla es tornarà a GameFinishedScreen si l'enviament ha estat
     * exitós.
     *
     * @param context El context de l'aplicació.
     * @param emailContent El contingut del correu electrònic que s'enviarà.
     * @param recipientEmail L'email del destinatari.
     */
    private fun sendEmail(context: Context, emailContent: String, recipientEmail: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, "Detalls de la partida")
            putExtra(Intent.EXTRA_TEXT, emailContent)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar correu electrònic"))
    }
}
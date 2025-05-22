package com.mb.spacegame

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.mb.spacegame.databinding.FragmentGameScreenBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameScreenFragment : Fragment() {

    private lateinit var binding: FragmentGameScreenBinding
    private val gameViewModel: GameViewModel by lazy {
        ViewModelProvider(requireActivity()).get(GameViewModel::class.java)
    }
    private lateinit var planets: List<Planets>
    private lateinit var spaceshipController: SpaceshipController
    private lateinit var navController: NavController
    private var planetAnimators: MutableMap<ImageView, ObjectAnimator> = mutableMapOf()
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameScreenBinding.inflate(inflater, container, false)
        mediaPlayer = MediaPlayer.create(context, R.raw.game)
        mediaPlayer?.start()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navController = findNavController()
        initializePlanets()
        setupSpaceshipController()
        setupQuizButton()
        observeViewModel()
        setupPlanetAnimations()

    }

    private fun initializePlanets() {
        planets = listOf(
            Planets("Mercury", binding.mercury, """
            Mercury has a rocky surface covered in craters, just like
            Earth’s Moon, and has temperatures which can reach
            up to 350°C on its sunlit side and –170°C on its dark
            side. Mercury has no atmosphere, so the sky appears
            dark all the time.
        """.trimIndent()),

            Planets("Venus", binding.venus, """
            Venus is the only inner planet in the Solar System to turn
            in a clockwise direction on its axis. Venus is covered
            in poisonous clouds containing sulphuric acid, and an
            atmosphere containing mainly carbon dioxide. Venus’
            surface remained a mystery because of its thick cloud
            cover until 1990-1994 when radar imaging equipment
            on the Magellan space craft managed to look through
            the clouds to reveal a rocky and volcanic surface.
        """.trimIndent()),

            Planets("Earth", binding.earth, """
            Earth is the only planet in the Solar System known to
            contain life. Earth is the only planet to contain water in
            its three forms, as a solid (ice), as a liquid (sea, rain, etc.)
            and as a gas (steam, clouds, atmosphere). It has a rocky
            surface.
        """.trimIndent()),

            Planets("Mars", binding.mars, """
            Mars is a small red, rocky planet with a very thin
            atmosphere of carbon dioxide. It is believed that it once
            had flowing water on its surface. The planet has a rusty
            surface and a pink sky. It is covered in rocks and impact
            craters.
        """.trimIndent()),

            Planets("Jupiter", binding.jupiter, """
            Jupiter is the biggest planet in the Solar System. The
            planet is a gas giant, made up mainly of hydrogen and
            helium. Its main feature is a Great Red Spot, which is a
            storm that has been going on for hundreds of years. It
            has a faint ring system around it.
        """.trimIndent()),

            Planets("Saturn", binding.saturn, """
            Like Jupiter, Saturn is also made up mainly of hydrogen
            and helium. Saturn’s most famous feature is its great
            rings. These are rings of small dust, rock and ice
            particles, probably what remains of a shattered moon
            which once orbited Saturn.
        """.trimIndent()),

            Planets("Uranus", binding.uranus, """
            Uranus, a gas giant, has an atmosphere of hydrogen,
            helium and methane. The methane gives the planet a
            pale blue colour. The planet is tipped on its side, possibly
            from a collision with an object the size of Earth. It is so
            cold that some of the gas is frozen. Uranus has rings of
            ice and small rock particles. However, these rings are
            too faint to be seen from Earth.
        """.trimIndent()),

            Planets("Neptune", binding.neptune, """
            Often considered to be a twin planet of Uranus, Neptune
            is a similar size and has similar composition. The winds
            on the planet are the strongest in the Solar System, with
            areas of high pressure shown by dark spots. Clouds of
            icy droplets of methane can also be seen in the upper
            atmosphere of Neptune. It also has a very faint ring
            system.
        """.trimIndent()),

            Planets("The Sun", binding.sun, """
            The Sun is a huge ball of gas which provides energy to
            its planets. It contains 99% of all of the matter in the
            Solar System and, at 4.5 billion years old, is about half
            way through its life. Without the Sun, life on Earth
            simply wouldn’t exist. The Sun is just one of billions
            of stars in the Milky Way galaxy and is the closest star
            to Earth. Between the orbits of Mars and Jupiter is an
            area called the Asteroid Belt. Occasionally asteroids
            collide with each other, and may, one day, in millions
            of years, all join together to form another Earth-sized
            planet. The Solar System is constantly developing,
            and the Asteroid Belt may be a planet still in production.
        """.trimIndent())
        )
    }


    private fun setupSpaceshipController() {
        spaceshipController = SpaceshipController(binding.spaceshipImage, planets, gameViewModel, binding.gameLayout).apply {
            enableControls()
            allPlanetsGoneListener = object : SpaceshipController.OnAllPlanetsGoneListener {
                override fun onAllPlanetsGone() {
                    binding.quizButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupQuizButton() {
        binding.quizButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            navController.navigate(R.id.action_gameScreenFragment_to_quizScreenFragment)
        }
    }

    private fun observeViewModel() {
        gameViewModel.gameLiveModel.observe(viewLifecycleOwner, { gameModel ->
            binding.playerName.text = gameModel?.name ?: "No Name"
            if (gameModel.gameState == GameState.Paused) pauseGameAnimations() else resumeGameAnimations()
        })
    }

    private fun setupPlanetAnimations() {
        binding.gameLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.gameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                planets.forEach { animatePlanet(it.imageView, binding.gameLayout) }
            }
        })
    }

    private fun pauseGameAnimations() {
        planetAnimators.values.forEach { it.pause() }
    }

    private fun resumeGameAnimations() {
        planetAnimators.values.forEach { it.resume() }
    }

    private fun animatePlanet(planet: ImageView, container: ConstraintLayout) {

        val movementRange = container.width.toFloat() / 2 - planet.width / 2
        val startValue = -movementRange
        val animator = ObjectAnimator.ofFloat(planet, "translationX", startValue, movementRange).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        planetAnimators[planet] = animator
        animator.start()

    }

    companion object {
        @JvmStatic
        fun newInstance() = GameScreenFragment()
    }
}


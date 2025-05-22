package com.mb.spacegame

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mb.spacegame.databinding.FragmentQuizScreenBinding
import android.app.AlertDialog
import android.media.MediaPlayer


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuizScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuizScreenFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentQuizScreenBinding
    private val questions = arrayOf("Which of the following statements is true about Neptune?",
        "What is a distinctive feature of Uranus?",
        "What is the most famous feature of Saturn?",
        "What is a notable feature of Jupiter?",
        "What is a characteristic feature of Mars?",
        "What is unique about Earth in the Solar System?",
        "What is a distinctive characteristic of Venus?",
        "What is a notable characteristic of Mercury?",
        "What role does the Sun play in the Solar System, and what is the Asteroid Belt's potential significance?")

    private val options = arrayOf(
        arrayOf("Neptune is the smallest planet in the Solar System.", "Neptune has the strongest winds in the Solar System.", "Neptune is primarily composed of iron and rock."),
        arrayOf("Uranus has a deep red color due to its high iron content.", "Uranus is the hottest planet in the Solar System due to its dense atmosphere.", "Uranus is tipped on its side, possibly due to a collision with an Earth-sized object."),
        arrayOf("Saturn is known for its large, fiery volcanoes that are active year-round.", "Saturn's most famous feature is its great rings made of dust, rock, and ice particles.", "Saturn is unique for having the most moons of any planet in the Solar System."),
        arrayOf("Jupiter is known for its Great Red Spot, a storm that has been ongoing for hundreds of years.", "Jupiter has the most extensive ring system in the Solar System.", "Jupiter is primarily known for its solid, rocky surface."),
        arrayOf("Mars is distinguished by its ring system and numerous moons.", "Mars has a very thin atmosphere primarily composed of carbon dioxide.", "Mars is known for its large oceans covering most of its surface."),
        arrayOf("Earth is the only planet with a surface completely covered in water.", "Earth is the only planet with a purely nitrogen-based atmosphere.", "Earth is the only planet known to contain life and has water in its three forms: solid, liquid, and gas."),
        arrayOf("Venus is the only inner planet to rotate in a clockwise direction on its axis.", "Venus has the clearest atmosphere in the Solar System, making it easy to observe from Earth.", "Venus is known for its large, water-based oceans covering its surface."),
        arrayOf("Mercury has a thick atmosphere that produces a colorful sky.", "Mercury's surface is covered in liquid oceans.", "Mercury lacks an atmosphere, resulting in a dark sky all the time."),
        arrayOf("The Sun is a small star in the Milky Way galaxy, and the Asteroid Belt is a fully formed planet.", "The Sun provides energy to its planets and is essential for life on Earth, while the Asteroid Belt may eventually form another Earth-sized planet in millions of years.", "The Sun is the closest star to Earth, and the Asteroid Belt is a stable region with no changes over time.")

    )

    private val correctAnswers = arrayOf(1,2,1,0,1,2,0,2,1)

    private var currentQuestionIndex = 0
    private var score = 0
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentQuizScreenBinding.inflate(inflater, container, false)
        mediaPlayer = MediaPlayer.create(context, R.raw.quiz)
        mediaPlayer?.start()
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayQuestion()

        binding.option1Button.setOnClickListener{
            checkAnswer(0)
        }
        binding.option2Button.setOnClickListener {
            checkAnswer(1)
        }
        binding.option3Button.setOnClickListener {
            checkAnswer(2)
        }
        binding.restartQuiz.setOnClickListener {
            restartQuiz()
        }
    }

    private fun correctButtonColours(buttonIndex: Int){
        when(buttonIndex){
            0 -> binding.option1Button.setBackgroundColor(Color.GREEN)
            1 -> binding.option2Button.setBackgroundColor(Color.GREEN)
            2 -> binding.option3Button.setBackgroundColor(Color.GREEN)
        }
    }

    private fun wrongButtonColours(buttonIndex: Int){
        when(buttonIndex){
            0 -> binding.option1Button.setBackgroundColor(Color.RED)
            1 -> binding.option2Button.setBackgroundColor(Color.RED)
            2 -> binding.option3Button.setBackgroundColor(Color.RED)
        }
    }

    private fun resetButtonColours(){
        binding.option1Button.setBackgroundColor(Color.rgb(50, 59, 96))
        binding.option2Button.setBackgroundColor(Color.rgb(50, 59, 96))
        binding.option3Button.setBackgroundColor(Color.rgb(50, 59, 96))
    }

    /*private fun showResults() {
        Toast.makeText(requireContext(), "Your score: $score out of ${questions.size}", Toast.LENGTH_LONG).show()
        binding.restartQuiz.isEnabled = true
    }*/

    private fun showResults() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Quiz Completed")
        dialogBuilder.setMessage("Your score: $score out of ${questions.size}")
        dialogBuilder.setCancelable(false)

        // Restart Quiz Button
        dialogBuilder.setPositiveButton("Restart Quiz") { dialog, _ ->
            restartQuiz()
            dialog.dismiss()
        }

        // Restart App Button
        dialogBuilder.setNeutralButton("Restart App") { dialog, _ ->
            // Intent to restart your app
            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        // Close App Button
        dialogBuilder.setNegativeButton("Close App") { _, _ ->
            requireActivity().finish() // Close the activity
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        val dialog = dialogBuilder.create()
        dialog.show()


    }


    private fun displayQuestion(){
        binding.questionText.text = questions[currentQuestionIndex]
        binding.option1Button.text = options[currentQuestionIndex][0]
        binding.option2Button.text = options[currentQuestionIndex][1]
        binding.option3Button.text = options[currentQuestionIndex][2]
        resetButtonColours()
    }

    private fun checkAnswer(selectedAnswerIndex: Int) {
        val correctAnswerIndex = correctAnswers[currentQuestionIndex]

        if (selectedAnswerIndex == correctAnswerIndex) {
            score++
            correctButtonColours(selectedAnswerIndex)
        } else {
            wrongButtonColours(selectedAnswerIndex)
            correctButtonColours(correctAnswerIndex)
        }

        if (currentQuestionIndex < questions.size - 1)
        {
            currentQuestionIndex++
            binding.questionText.postDelayed({displayQuestion()}, 1000)
        } else {
            showResults()
        }
    }

    private fun restartQuiz(){
        currentQuestionIndex = 0
        score = 0
        displayQuestion()
        binding.restartQuiz.isEnabled = false
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuizScreenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuizScreenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
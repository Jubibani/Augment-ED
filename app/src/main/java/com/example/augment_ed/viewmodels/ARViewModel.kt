import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.augment_ed.data.ConceptRepository
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Concept(
    val term: String,
    val description: String,
    val modelPath: String,
    val pronunciationGuide: String?
)

class ARViewModel(private val repository: ConceptRepository) : ViewModel() {
    private val _recognizedTerm = MutableStateFlow<String?>(null)
    val recognizedTerm: StateFlow<String?> = _recognizedTerm

    private val _currentConcept = MutableStateFlow<Concept?>(null)
    val currentConcept: StateFlow<Concept?> = _currentConcept

    private lateinit var concepts: List<Concept>

    fun loadConcepts(context: Context) {
        viewModelScope.launch {
            val jsonString = context.assets.open("concepts.json").bufferedReader().use { it.readText() }
            concepts = Gson().fromJson(jsonString, object : TypeToken<List<Concept>>() {}.type)
        }
    }

    fun recognizeTerm(term: String) {
        _recognizedTerm.value = term
        _currentConcept.value = concepts.find { it.term.equals(term, ignoreCase = true) }
    }

    fun loadModelForConcept(context: Context, onModelLoaded: (ModelRenderable) -> Unit) {
        val concept = _currentConcept.value ?: return
        val modelPath = concept.modelPath
        ModelRenderable.builder()
            .setSource(context, android.net.Uri.parse(modelPath))
            .build()
            .thenAccept { renderable ->
                onModelLoaded(renderable)
            }
            .exceptionally { throwable ->
                // Handle any errors
                Log.e("ARViewModel", "Error loading model", throwable)
                null
            }
    }

    fun startARScan() {
        viewModelScope.launch {
            // Start AR session
            // This is where you would typically:
            // 1. Ensure the AR session is set up
            // 2. Start the camera preview
            // 3. Begin processing frames for text recognition
            // 4. Handle recognized text and match with concepts
            
            Log.d("ARViewModel", "Starting AR scan")
            
            // TODO: Implement actual AR scanning logic
            // For now, we'll just log a message and fetch a random concept as an example
            val randomConcept = repository.getRandomConcept()
            if (randomConcept != null) {
                Log.d("ARViewModel", "Random concept: ${randomConcept.term}")
                recognizeTerm(randomConcept.term)
            } else {
                Log.d("ARViewModel", "No concepts found")
            }
        }
    }
}
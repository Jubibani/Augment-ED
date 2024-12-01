import android.content.Context
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

class ARViewModel(private val repository: ConceptRepository)  {
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
                null
            }
    }
}
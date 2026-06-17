package ch.admin.DidResolverDemoApp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.admin.eid.didresolver.did_sidekicks.DidDocExtended
import ch.admin.eid.didresolver.didresolver.Did
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext("name"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            scope.launch {
                onClick()
            }
        }
        scope.launch {
            onClick()
        }
    }

    // Resolves the provided did and returns the did document
    fun resolve(did: String): DidDocExtended {
        // Resolve did to did doc
        val didObj = Did(did) // may throw DidResolveException

        // make an HTTP GET request to get the did log
        val url = didObj.getHttpsUrl() // may throw DidResolveException
        val didLog = fetchDidLog(url)

        val didDoc = didObj.resolveAll(didLog)
        didObj.close()
        return didDoc
    }

    // Does an https request to retrieve the did log from the server
    @Throws(IOException::class, URISyntaxException::class)
    private fun fetchDidLog(url: String): String {
        val content = StringBuilder()

        val connection = (URI(url).toURL().openConnection() as HttpsURLConnection)
        connection.inputStream.bufferedReader().use { reader ->
            var inputLine: String?
            while (reader.readLine().also { inputLine = it } != null) {
                content.append(inputLine)
            }
        }

        return content.toString()
    }

    fun onClick() {
        val textView: TextView = findViewById(R.id.textView)
        val progressBar: ProgressBar =  findViewById(R.id.progressBar)
        val input: EditText =  findViewById(R.id.editText)
        val did = input.text.toString()

        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            textView.text = getString(R.string.loading)
        }

        var text: String
        try {
            val doc = resolve(did)
            text = "VerificationMethods:\n" + doc.getDidDoc().getVerificationMethod().map { it.id }.joinToString("\n")
        } catch (e: Exception) {
            Log.e("DidResolver Error", e.toString())
            text = "Something went wrong. See logs for more details."
        }

        runOnUiThread {
            textView.text = text
            progressBar.visibility = View.INVISIBLE
        }
    }
}
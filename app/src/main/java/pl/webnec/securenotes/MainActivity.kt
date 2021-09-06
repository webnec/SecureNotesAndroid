package pl.webnec.securenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pl.webnec.securenotes.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = MainViewModel()
    }

    override fun onStop() {
        super.onStop()
        finishAffinity()
    }
}
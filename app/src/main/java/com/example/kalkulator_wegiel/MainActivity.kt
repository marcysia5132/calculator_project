package com.example.kalkulator_wegiel

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var okienko: TextView
    private lateinit var wynik: TextView
    private var canAddOperation = false
    private var isResultDisplayed = false // Flaga określająca, czy pokazano wynik
    var DOTFLAG = 0 // definicja flagi kropki

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //inicjalizacja
        okienko = findViewById(R.id.okienko)
        wynik = findViewById(R.id.wynik)
        val buttonPi: Button = findViewById(R.id.button_pi)
        buttonPi.setOnClickListener {
            if (isResultDisplayed) {
                okienko.text = Math.PI.toString()
                isResultDisplayed = false
            } else {
                okienko.append(Math.PI.toString())
            }
            DOTFLAG = 1 // Ustaw flagę kropki, ponieważ liczba Pi zawiera kropkę
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun numeraction(view: View) {
        if (view is Button) {
            if (isResultDisplayed) {
                okienko.text = ""
                isResultDisplayed = false
            }

            val currentText = okienko.text.toString()
            val lastNumber = currentText.split('+', '-', '*', ':').lastOrNull() ?: ""

            // Uniemożliwienie dodania kropki, jeśli w ostatniej liczbie już jest
            if (view.text == "." && (lastNumber.isEmpty() || DOTFLAG == 1)) {
                return // Ignorujemy kliknięcie kropki
            }

            // Dodaj tekst przy braku błędów
            okienko.append(view.text)
            canAddOperation = true
        }
    }


    fun operatoraction(view: View) {
        if (view is Button) {
            if (view.text == "mod") {
                okienko.append("%") // Zamieniamy "mod" na "%"
            } else {
                okienko.append(view.text)

                if (isResultDisplayed) {
                    okienko.text = wynik.text
                    isResultDisplayed = false
                }
                if (canAddOperation) {
                    okienko.append(view.text)
                    canAddOperation = false
                }
            }
        }
    }

    fun usundozeraction(view: View) {
        DOTFLAG = 0
        okienko.text = ""
        wynik.text = ""
        isResultDisplayed = false
    }

    fun backSpaceaction(view: View) {
        val length = okienko.length()
        if (length > 0) {
            okienko.text = okienko.text.subSequence(0, length - 1)
        }
        canAddOperation = true
    }

    fun rownasieaction(view: View) {
        try {
            val result = obliczwynik()
            wynik.text = formatujWynik(result)
            isResultDisplayed = true
        } catch (e: ArithmeticException) {
            wynik.text = "Błąd"
            isResultDisplayed = true
        } catch (e: Exception) {
            wynik.text = "Błąd"
            println("wystąpił błąd: ".format(e))
            isResultDisplayed = true
        }
    }

    private fun obliczwynik(): Double {
        val cyfraoperator = cyfraoperator()
        if (cyfraoperator.isEmpty()) return 0.0

        val PomnozPodziel = PomnozPodziel(cyfraoperator)
        if (PomnozPodziel.isEmpty()) return 0.0

        return dodajOdejmijOblicz(PomnozPodziel)
    }


    private fun dodajOdejmijOblicz(zalList: MutableList<Any>): Double {
        var wynikKoniec = zalList[0] as Double

        for (i in zalList.indices) {
            if (zalList[i] is Char && i != zalList.lastIndex) {
                val operator = zalList[i]
                val nastcyfra = zalList[i + 1] as Double
                if (operator == '+') wynikKoniec += nastcyfra
                if (operator == '-') wynikKoniec -= nastcyfra
            }
        }
        return wynikKoniec
    }

    private fun PomnozPodziel(zalLista: MutableList<Any>): MutableList<Any> {
        var lista = zalLista
        while (lista.contains('*') || lista.contains(':')) {
            lista = obliczPomnozPodziel(lista)
        }
        return lista
    }

    private fun obliczPomnozPodziel(zalLista: MutableList<Any>): MutableList<Any> {
        val nowaLista = mutableListOf<Any>()
        var restartIndex = zalLista.size

        for (i in zalLista.indices) {
            if (zalLista[i] is Char && i != zalLista.lastIndex && i < restartIndex) {
                val operator = zalLista[i] as Char
                val poprzcyfra = zalLista[i - 1] as Double
                val nastcyfra = zalLista[i + 1] as Double
                when (operator) {
                    '*' -> {
                        nowaLista.add(poprzcyfra * nastcyfra)
                        restartIndex = i + 1
                    }

                    ':' -> {
                        if (nastcyfra == 0.0) throw ArithmeticException("BŁĄD: dzielenie przez 0")
                        nowaLista.add(poprzcyfra / nastcyfra)
                        restartIndex = i + 1
                    }

                    '%' -> {
                        nowaLista.add(poprzcyfra % nastcyfra)
                        restartIndex = i + 1
                    }

                    else -> {
                        nowaLista.add(poprzcyfra)
                        nowaLista.add(operator)
                    }
                }
            }
            if (i > restartIndex)
                nowaLista.add(zalLista[i])
        }
        return nowaLista
    }

    //tworzy liste wpisanych znaków
    private fun cyfraoperator(): MutableList<Any> {
        val lista = mutableListOf<Any>()
        var aktualnacyfra = ""
        var isNegative = false

        for (i in okienko.text.indices) {
            val character = okienko.text[i]

            when {
                character.isDigit() -> {
                    aktualnacyfra += character
                }
                character == '.' -> {
                    if (!aktualnacyfra.contains('.')) { // Dodaj kropkę, jeśli nie występuje
                        aktualnacyfra += character
                    }
                }
                character == '-' && (i == 0 || okienko.text[i - 1] in listOf('+', '-', '*', ':', '(')) -> {
                    isNegative = true
                }
                character == '%' -> {
                    // Rozpoznaj "mod" jako operator
                    if (aktualnacyfra.isNotEmpty()) {
                        lista.add(if (isNegative) -aktualnacyfra.toDouble() else aktualnacyfra.toDouble())
                        aktualnacyfra = ""
                        isNegative = false
                    }
                    lista.add('%') // Dodaj operator "mod"
                }
                else -> {
                    if (aktualnacyfra.isNotEmpty()) {
                        lista.add(if (isNegative) -aktualnacyfra.toDouble() else aktualnacyfra.toDouble())
                        aktualnacyfra = ""
                        isNegative = false
                    }
                    lista.add(character)
                }
            }
        }

        if (aktualnacyfra.isNotEmpty()) {
            lista.add(if (isNegative) -aktualnacyfra.toDouble() else aktualnacyfra.toDouble())
        }

        return lista
    }


    private fun formatujWynik(wynik: Double): String {
        val bigDecimalWynik = BigDecimal(wynik).setScale(15, RoundingMode.HALF_UP)
        return if (bigDecimalWynik.stripTrailingZeros().scale() <= 0) {
            bigDecimalWynik.toBigInteger().toString()
        } else {
            bigDecimalWynik.stripTrailingZeros().toPlainString()
        }
    }

    fun zmienZnak(view: View) {
        // Pobierz bieżący tekst z okienka
        val currentText = okienko.text.toString()

        // Sprawdź, czy liczba ma już znak - na początku
        if (currentText.isNotEmpty()) {
            // Jeśli liczba zaczyna się od "-", usuń go, zmieniając na "+"
            if (currentText[0] == '-') {
                okienko.text = currentText.substring(1) // Usuń znak "-"
            } else {
                // Jeśli liczba nie zaczyna się od "-", dodaj "-" na początku
                okienko.text = "-$currentText"
            }
        }
    }

    fun procentaction(view: View) {
        try {
            val currentText = okienko.text.toString()

            if (currentText.isNotEmpty()) {
                val number = currentText.toDouble()
                val result = number * 0.01
                wynik.text = formatujWynik(result) // Poprawne formatowanie wyniku
                isResultDisplayed = true
                DOTFLAG = if (result % 1 == 0.0) 0 else 1 // Ustaw flagę kropki w zależności od wyniku
            }
        } catch (e: Exception) {
            wynik.text = "Błąd"
            isResultDisplayed = true
        }
    }

    fun pierwiastekaction(view: View) {
        try {
            val currentText = okienko.text.toString()

            if (currentText.isNotEmpty()) {
                val number = currentText.toDouble()

                if (number >= 0) {
                    val result = kotlin.math.sqrt(number)
                    wynik.text = formatujWynik(result) // Poprawne formatowanie wyniku
                    isResultDisplayed = true
                    DOTFLAG = if (result % 1 == 0.0) 0 else 1 // Ustaw flagę kropki w zależności od wyniku
                } else {
                    wynik.text = "Błąd: liczba ujemna"
                    isResultDisplayed = true
                }
            }
        } catch (e: Exception) {
            wynik.text = "Błąd"
            isResultDisplayed = true
        }
    }
}

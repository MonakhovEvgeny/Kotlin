import java.io.File


sealed interface Command {
    fun isValid(): Boolean
}

data class AddPhoneCommand(val name: String, val phone: String) : Command {
    override fun isValid(): Boolean {
        val phonePattern = Regex("^\\+[0-9]+\$")
        return phone.matches(phonePattern)
    }
}

data class AddEmailCommand(val name: String, val email: String) : Command {
    override fun isValid(): Boolean {
        val emailPattern = Regex("^[A-Za-z]+@[A-Za-z]+\\.[A-Za-z]+$")
        return email.matches(emailPattern)
    }
}

data class ShowCommand(val name: String) : Command {
    override fun isValid(): Boolean = name.isNotBlank()
}

data class FindCommand(val query: String) : Command {
    override fun isValid(): Boolean = query.isNotBlank()
}

data class ExportCommand(val filePath: String) : Command {
    override fun isValid(): Boolean = filePath.isNotBlank()
}

data object ExitCommand : Command {
    override fun isValid(): Boolean = true
}

data object HelpCommand : Command {
    override fun isValid(): Boolean = true
}


data class Person(
    val name: String,
    val phones: MutableList<String> = mutableListOf(),
    val emails: MutableList<String> = mutableListOf()
)


class JsonObject {
    private val properties = mutableListOf<Pair<String, String>>()

    fun addProperty(name: String, value: String) {
        properties.add(name to "\"$value\"")
    }

    fun addArrayProperty(name: String, array: JsonArray) {
        properties.add(name to array.toString())
    }

    override fun toString(): String {
        return properties.joinToString(prefix = "{", postfix = "}") { "\"${it.first}\": ${it.second}" }
    }
}

class JsonArray {
    private val items = mutableListOf<String>()

    fun addItem(value: String) {
        items.add("\"$value\"")
    }

    fun addObjectItem(obj: JsonObject) {
        items.add(obj.toString())
    }

    override fun toString(): String {
        return items.joinToString(prefix = "[", postfix = "]")
    }
}

fun jsonObject(init: JsonObject.() -> Unit): JsonObject {
    val obj = JsonObject()
    obj.init()
    return obj
}

fun jsonArray(init: JsonArray.() -> Unit): JsonArray {
    val array = JsonArray()
    array.init()
    return array
}


fun readCommand(): Command {
    val input = readlnOrNull() ?: return HelpCommand

    val parts = input.split(" ")
    return when {
        input == "exit" -> ExitCommand
        input == "help" -> HelpCommand
        parts.size == 2 && parts[0] == "show" -> ShowCommand(parts[1])
        parts.size == 2 && parts[0] == "find" -> FindCommand(parts[1])
        input.startsWith("export ") -> ExportCommand(input.removePrefix("export ").trim())
        parts.size == 4 && parts[0] == "add" && parts[2] == "phone" -> AddPhoneCommand(parts[1], parts[3])
        parts.size == 4 && parts[0] == "add" && parts[2] == "email" -> AddEmailCommand(parts[1], parts[3])
        else -> HelpCommand
    }
}



fun exportToJsonFile(phoneBook: Map<String, Person>, filePath: String) {
    val jsonArray = jsonArray {
        phoneBook.values.forEach { person ->
            addObjectItem(
                jsonObject {
                    addProperty("name", person.name)
                    addArrayProperty("phones", jsonArray {
                        person.phones.forEach { addItem(it) }
                    })
                    addArrayProperty("emails", jsonArray {
                        person.emails.forEach { addItem(it) }
                    })
                }
            )
        }
    }

    val file = File(filePath)
    try {
        file.writeText(jsonArray.toString())
        println("Данные успешно экспортированы в файл: $filePath")
    } catch (e: Exception) {
        println("Ошибка: Не удалось записать данные в файл. ${e.message}")
    }
}



fun main() {
    val phoneBook = mutableMapOf<String, Person>()

    println("Введите команду (для справки введите 'help'): ")

    while (true) {
        print("> ")
        val command = readCommand()

        println("Команда: $command")

        if (!command.isValid()) {
            println("Ошибка: Неверные аргументы команды.")
            println("Доступные команды:")
            println("exit - выход из программы")
            println("help - помощь")
            println("add <Имя> phone <Номер телефона> - добавить номер телефона")
            println("add <Имя> email <Адрес электронной почты> - добавить адрес электронной почты")
            println("show <Имя> - показать телефоны и email для человека")
            println("find <Телефон или Email> - найти человека по телефону или email")
            println("export <Путь к файлу> - экспортировать данные в файл JSON")
            continue
        }

        when (command) {
            is AddPhoneCommand -> {
                val person = phoneBook.getOrPut(command.name) { Person(name = command.name) }
                person.phones.add(command.phone)
                println("Добавлен номер телефона: Имя: ${command.name}, Телефон: ${command.phone}")
            }
            is AddEmailCommand -> {
                val person = phoneBook.getOrPut(command.name) { Person(name = command.name) }
                person.emails.add(command.email)
                println("Добавлен адрес электронной почты: Имя: ${command.name}, Email: ${command.email}")
            }
            is ShowCommand -> {
                val person = phoneBook[command.name]
                if (person != null) {
                    println("Имя: ${person.name}")
                    println("Телефоны: ${person.phones.joinToString()}")
                    println("Email: ${person.emails.joinToString()}")
                } else {
                    println("Человек с именем ${command.name} не найден.")
                }
            }
            is FindCommand -> {
                val results = phoneBook.values.filter {
                    it.phones.contains(command.query) || it.emails.contains(command.query)
                }
                if (results.isNotEmpty()) {
                    println("Найдены люди:")
                    results.forEach { println(it.name) }
                } else {
                    println("Не найдено ни одного человека с указанным телефоном или email.")
                }
            }
            is ExportCommand -> {
                exportToJsonFile(phoneBook, command.filePath)
            }
            is ExitCommand -> {
                println("Выход из программы.")
                break
            }
            is HelpCommand -> {
                println("Доступные команды:")
                println("exit - выход из программы")
                println("help - помощь")
                println("add <Имя> phone <Номер телефона> - добавить номер телефона")
                println("add <Имя> email <Адрес электронной почты> - добавить адрес электронной почты")
                println("show <Имя> - показать телефоны и email для человека")
                println("find <Телефон или Email> - найти человека по телефону или email")
                println("export <Путь к файлу> - экспортировать данные в файл JSON")
            }
        }
    }
}

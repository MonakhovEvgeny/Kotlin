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

object ExitCommand : Command {
    override fun isValid(): Boolean = true
}

object HelpCommand : Command {
    override fun isValid(): Boolean = true
}

object ShowCommand : Command {
    override fun isValid(): Boolean = true
}
fun readCommand(): Command {
    val input = readlnOrNull() ?: return HelpCommand

    val parts = input.split(" ")
    return when {
        input == "exit" -> ExitCommand
        input == "help" -> HelpCommand
        input == "show" -> ShowCommand
        parts.size == 4 && parts[0] == "add" && parts[2] == "phone" -> AddPhoneCommand(parts[1], parts[3])
        parts.size == 4 && parts[0] == "add" && parts[2] == "email" -> AddEmailCommand(parts[1], parts[3])
        else -> HelpCommand
    }
}
data class Person(val name: String, val phone: String? = null, val email: String? = null)


fun main() {
    var lastPerson: Person? = null

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
            println("show - показать последнее введённое значение")
            continue
        }

        when (command) {
            is AddPhoneCommand -> {
                lastPerson = Person(name = command.name, phone = command.phone)
                println("Добавлен номер телефона: Имя: ${command.name}, Телефон: ${command.phone}")
            }
            is AddEmailCommand -> {
                lastPerson = Person(name = command.name, email = command.email)
                println("Добавлен адрес электронной почты: Имя: ${command.name}, Email: ${command.email}")
            }
            is ShowCommand -> {
                if (lastPerson != null) {
                    println("Последнее значение: $lastPerson")
                } else {
                    println("Not initialized")
                }
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
                println("show - показать последнее введённое значение")
            }
        }
    }
}

//
//


import kotlin.random.Random

// Определение классов для различных видов транспорта
abstract class Transport(val costPerWeight: Double, val speed: Double) {
    abstract fun canOperate(from: City, to: City): Boolean
}

class CarTransport(costPerWeight: Double, speed: Double) : Transport(costPerWeight, speed) {
    override fun canOperate(from: City, to: City) = true
}

class TrainTransport(costPerWeight: Double, speed: Double) : Transport(costPerWeight, speed) {
    override fun canOperate(from: City, to: City) = from.size >= CitySize.Medium && to.size >= CitySize.Medium
}

class AirTransport(costPerWeight: Double, speed: Double) : Transport(costPerWeight, speed) {
    override fun canOperate(from: City, to: City) = from.size == CitySize.Large && to.size == CitySize.Large && from.weather == Weather.Good && to.weather == Weather.Good
}

// Определение классов для городов и погоды
enum class CitySize { Small, Medium, Large }
enum class Weather { Good, Bad }

data class City(val name: String, val size: CitySize, var weather: Weather)

// Определение класса для заказа
data class Order(val from: City, val to: City, val weight: Double, val clientWishes: ClientWishes)

// Определение класса для пожеланий клиента
data class ClientWishes(val preferredSpeed: Double?, val preferredCost: Double?)

// Добавление функциональности для обработки заказов и учета доходов и расходов в класс TransportAgency
class TransportAgency(val transports: List<Transport>) {
    var income = 0.0
    var expenses = 0.0

    fun processOrder(order: Order): Transport? {
        // Найти подходящий вид транспорта
        val suitableTransports = transports.filter { it.canOperate(order.from, order.to) }
        // Если нет подходящего транспорта, вернуть null
        if (suitableTransports.isEmpty()) {
            println("Нет подходящего транспорта для перевозки из ${order.from.name} в ${order.to.name}")
            return null
        }

        // Выбрать наиболее подходящий вид транспорта с учетом пожеланий клиента
        val preferredTransports = suitableTransports.filter {
            (order.clientWishes.preferredSpeed == null || it.speed >= order.clientWishes.preferredSpeed) &&
                    (order.clientWishes.preferredCost == null || it.costPerWeight * order.weight <= order.clientWishes.preferredCost)
        }

        val transport = if (preferredTransports.isNotEmpty()) {
            preferredTransports.minByOrNull { it.costPerWeight * order.weight }
        } else {
            suitableTransports.minByOrNull { it.costPerWeight * order.weight }
        }

        // Расчет доходов и расходов
        if (transport != null) {
            income += transport.costPerWeight * order.weight
            expenses += transport.costPerWeight * order.weight / 2  // предположим, что расходы составляют половину доходов

            // Учет погодных условий и аварийности
            if (transport !is AirTransport && (order.from.weather == Weather.Bad || order.to.weather == Weather.Bad)) {
                expenses += transport.costPerWeight * order.weight * 1.2  // дополнительные расходы из-за плохой погоды
                println("Плохая погода увеличивает расходы")
            }

            if (Random.nextDouble() < 0.01) {  // предположим, что вероятность аварии составляет 1%
                expenses += transport.costPerWeight * order.weight * 2  // дополнительные расходы из-за аварии
                println("Авария увеличивает расходы")
            }

            println("Перевозка из ${order.from.name} в ${order.to.name}")
        } else {
            println("Не удалось найти подходящий транспорт для перевозки из ${order.from.name} в ${order.to.name}")
        }

        return transport
    }
}


fun main() {
    // Создание городов
    val moscow = City("Москва", CitySize.Large, Weather.Bad)
    val spb = City("Санкт-Петербург", CitySize.Large, Weather.Bad)
    val kazan = City("Казань", CitySize.Medium, Weather.Good)

    // Создание транспорта
    val car = CarTransport(100.0, 60.0)
    val train = TrainTransport(50.0, 80.0)
    val air = AirTransport(150.0, 500.0)

    // Создание транспортного агентства
    val agency = TransportAgency(listOf(car, train, air))

    // Создание заказа
    val order = Order(moscow, spb, 1000.0, ClientWishes(500.0, null))

    // Обработка заказа
    val transport = agency.processOrder(order)

    if (transport != null) {
        println("Выбран вид транспорта: ${transport::class.simpleName}")
        println("Стоимость перевозки: ${transport.costPerWeight * order.weight}")
        println("Доход агентства: ${agency.income}")
        println("Расходы агентства: ${agency.expenses}")
    } else {
        println("Перевозка невозможна")
    }
}

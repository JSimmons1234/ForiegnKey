case class Person(name: String, age: Int)

val people = List(Person("mac", 41), Person("Jo", 22))

people.filter(p=>p.age< 30)

people.map(p=>p.age).sum

people.foldLeft(0)((total, p) => total + p.age)
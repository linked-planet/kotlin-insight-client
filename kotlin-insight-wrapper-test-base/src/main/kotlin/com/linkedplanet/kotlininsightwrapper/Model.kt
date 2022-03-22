package com.linkedplanet.kotlininsightwrapper

enum class OBJECTS(val id: Int) {
    Company(1),
    Country(2),
    TestWithLists(34),
    SimpleObject(35),
    Many(65)
}

enum class MANY {
    Name
}

enum class COMPANY {
    Name,
    Country
}

enum class COUNTRY {
    Name,
    Key,
    ShortName
}

enum class TEST_WITH_LISTS {
    ItemList
}

enum class SIMPLE_OBJECT {
    Firstname,
}
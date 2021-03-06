/**
 * Copyright 2022 linked-planet GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedplanet.kotlininsightclient

enum class OBJECTS(val id: Int) {
    Company(1),
    Country(2),
    TestWithLists(34),
    SimpleObject(35),
    Many(65),
    Abstract(98)
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
/*
 * #%L
 * insight-reporting
 * %%
 * Copyright (C) 2018 The Plugin Authors
 * %%
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
 * #L%
 */
package com.linkedplanet.kotlininsightwrapper.api.interfaces

import arrow.core.Either
import com.linkedplanet.kotlininsightwrapper.api.model.InsightSchemas
import com.linkedplanet.kotlinhttpclient.error.DomainError

interface InsightSchemaOperatorInterface {

    suspend fun getSchemas(): Either<DomainError, InsightSchemas>

}
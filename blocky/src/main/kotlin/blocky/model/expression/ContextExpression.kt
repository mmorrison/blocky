/*
 * Copyright 2019 Donesky, LLC
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

package blocky.model.expression

import blocky.model.Context

internal class ContextExpression(private val name: String) : Expression {

    override val operator = Operator.And

    override fun evaluate(context: Context) =
        (context[name] as? ExpressionCallback)?.evaluate(context) ?: false
}

interface ExpressionCallback {

    fun evaluate(context: Context): Boolean
}
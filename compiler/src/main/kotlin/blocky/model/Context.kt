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
package blocky.model

import blocky.model.expression.NullValue

class Context(private val context: Map<String, Any?> = emptyMap()) {

    private val placeholders = mutableMapOf<String, Node>()
    private lateinit var parentContext: Context

    private constructor(context: Map<String, Any?>, parentContext: Context) : this(context) {
        this.parentContext = parentContext
    }

    internal fun getPlaceholder(name: String): Node = placeholders.getValue(name)

    internal fun setPlaceholder(name: String, node: Node) {
        placeholders[name] = node
    }

    operator fun get(name: String): Any? {
        val path = name.split(".")
        val itemName = path.first()
        if (!context.containsKey(itemName))
            return null
        if (path.size > 1) {
            val item = internalGet(itemName) ?: return NullValue
            val bean = BeanMap[item::class]
            return bean.get(item, path.subList(1, path.size))
        } else {
            return internalGet(name)
        }
    }

    private fun internalGet(name: String) =
        context[name] ?: if (::parentContext.isInitialized) parentContext[name] else null

    fun newChildContext(context: Map<String, Any?>): Context =
        Context(context, this)
}
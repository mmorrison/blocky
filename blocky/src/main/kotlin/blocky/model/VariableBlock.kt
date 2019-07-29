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

import blocky.Blocky
import java.io.OutputStream

internal open class VariableBlock(name: String, private val formatter: String?, private val formatterConfig: String?) : Node {

    private val contextName = name.substring(VariableBlock.contextPrefix.length)

    override fun write(context: Context, out: OutputStream) {
        formatter?.let {
            val formatter = Blocky.getFormatter(it)
            val output = formatter.format(context, formatterConfig, contextName)
            out.write(output)
        } ?: out.write(context[contextName].toString().toByteArray(Charsets.UTF_8))
    }

    override fun toString(): String {
        return "Variable(name=$contextName)"
    }

    companion object {

        const val contextPrefix = "ctx:"
    }
}
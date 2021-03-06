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
package blocky.model.builders

import blocky.compiler.CompilerException
import blocky.model.CompiledTemplate
import blocky.model.ElseBlock
import blocky.model.ForBlock
import blocky.model.IfBlock
import blocky.model.Node
import blocky.model.Placeholder
import blocky.model.PlaceholderRef
import blocky.model.TemplateRef
import blocky.model.VariableBlock
import java.nio.file.Path

internal open class BlockBuilder(private val path: Path) : NodeBuilder, NodeBuilderContainer {

    private val _children = mutableListOf<NodeBuilder>()

    var name: String? = null
    override val attributes = mutableMapOf<String, String>()

    override val children: List<NodeBuilder>
        get() = _children

    override fun addNode(node: NodeBuilder) {
        node.parent = this
        _children.add(node)
    }

    override var parent: NodeBuilder? = null

    private fun Node.build(children: MutableList<Node>) {
        _children.forEach {
            it.build(this)?.let { children.add(it) }
        }
    }

    override fun build(parent: Node): Node? {
        val name = name ?: throw CompilerException("Missing block name")
        return when {
            name == "root" -> {
                if (_children.size != 1) {
                    throw CompilerException("Invalid number of children: ${_children.size}")
                }
                _children.first().build(parent)
            }
            name == "template" -> {
                if (parent == RootBuilder.root) {
                    val children = mutableListOf<Node>()
                    val block = CompiledTemplate(path, children, attributes["parent"]?.let { path.resolveSibling(it) })
                    block.build(children)
                    block.validate()
                    block
                } else {
                    throw CompilerException("Unsupported template location")
                }
            }
            name == "placeholder" -> {
                val children = mutableListOf<Node>()
                val block = Placeholder(attributes.getValue("name"), children)
                block.build(children)
                block
            }
            name == "if" -> {
                val children = mutableListOf<Node>()
                val block = newIfBlock(parent, attributes["ctx"], children)
                block.build(children)
                block
            }
            name == "else" || name == "elseif" -> {
                val children = mutableListOf<Node>()
                val block = newElseBlock(parent, name, children)
                block.build(children)
                block
            }
            name == "for" -> {
                val children = mutableListOf<Node>()
                val variableName = attributes.entries.first()
                val block = ForBlock(variableName.key, variableName.value, children)
                block.build(children)
                block
            }
            name == "ref:template" -> TemplateRef(path, attributes["name"]?.let { path.resolveSibling(it) }, attributes["ctx"])
            name == "ref:placeholder" -> PlaceholderRef(attributes["name"], attributes["ctx"])
            name.startsWith(VariableBlock.contextPrefix) -> VariableBlock(
                name,
                attributes["format"],
                attributes["args"],
                attributes["default"]
            )
            else -> throw IllegalArgumentException("Unsupported block: $name")
        }
    }

    protected open fun newIfBlock(parent: Node, context: String?, children: List<Node>): IfBlock = TODO()

    protected open fun newElseBlock(parent: Node, name: String, children: List<Node>): ElseBlock = TODO()
}
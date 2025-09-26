package com.github.zimablue.devoutserver.feature.lamp

import net.minestom.server.entity.EntityType
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.minestom.actor.MinestomCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class EntityTypeParam: ParameterType<MinestomCommandActor,EntityType> {
    override fun parse(p0: MutableStringStream, p1: ExecutionContext<MinestomCommandActor>): EntityType? {
        return EntityType.fromKey(p0.readString().uppercase())
    }

    override fun defaultSuggestions(): SuggestionProvider<MinestomCommandActor> {
        return SuggestionProvider { EntityType.values().map { it.name().lowercase() } }
    }
}
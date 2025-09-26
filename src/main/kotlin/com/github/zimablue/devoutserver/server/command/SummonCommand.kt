package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.command.builder.condition.Conditions
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.*


@RegCommand
object SummonCommand : Command("summon") {
    private val argPos: ArgumentRelativeVec3 = ArgumentType.RelativeVec3("pos")
    private val argEntityClass: Argument<EntityClass> = ArgumentType.Enum("entityClass",EntityClass::class.java)
        .setFormat(ArgumentEnum.Format.LOWER_CASED)
        .setDefaultValue(EntityClass.CREATURE)
    private val argEntityType: Argument<EntityType> = ArgumentType.EntityType("entityType").setDefaultValue(EntityType.ZOMBIE)
    private val argTag: Argument<CompoundBinaryTag> = ArgumentType.NbtCompound("tag").setDefaultValue(CompoundBinaryTag.empty())

    init {
        setCondition{ sender, string ->
            return@setCondition Conditions.playerOnly(sender,string)&& sender.hasPermission("devoutserver.command.summon")
        }
        setDefaultExecutor { sender, context ->
            sender.sendMessage(Component.text("/summon [<entityType>] [<pos>] [<tag>] [<entityClass>]", NamedTextColor.GREEN))
        }
        addSyntax({ sender,context ->
            sender as Player
            val pos = context.get(argPos).from(sender).asPos()
            val entityClass = context.get(argEntityClass)
            val entityType = context.get(argEntityType)
            val tag = context.get(argTag)
            summon(sender,entityType,pos,tag,entityClass)
        }, argEntityType,argPos,argTag,argEntityClass)
    }

    private fun summon(player: Player, type: EntityType, pos: Pos, tag: CompoundBinaryTag?=null, entityClass: EntityClass=EntityClass.CREATURE) {
        val entity = entityClass.createEntity(type)
        tag?.let { entity.tagHandler().updateContent(entity.tagHandler().asCompound().put(it)) }
        entity.setInstance(player.instance,pos)
    }

    enum class EntityClass(private val factory: (EntityType) -> Entity) {
        BASE({ Entity(it) }),
        LIVING({ LivingEntity(it) }),
        CREATURE({ EntityCreature(it) });
        fun createEntity(type: EntityType) = factory.invoke(type)
    }
}
package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.LivingEntity
import net.minestom.server.utils.entity.EntityFinder
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.minestom.actor.MinestomCommandActor
@RegLamp
class NBTCommand {
    @LuckPermission("devoutserver.command.nbt")
    @Command("nbt")
    @Description("/nbt <target> [slot]")
    fun nbt(actor: MinestomCommandActor, target: EntityFinder, @Optional slot: EquipmentSlot?=null) {
        val sender = actor.sender()
        target.find(sender).forEach { entity ->
            val compound = if(slot==null) {
                entity.tagHandler().asCompound()
            } else {
                (entity as? LivingEntity)?.getEquipment(slot)?.toItemNBT()?:CompoundBinaryTag.empty()
            }
            sender.sendMessage(Component.text(compound.toString()))
        }
    }
}
package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import net.minestom.server.entity.Player
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.inventory.TransactionOption
import net.minestom.server.item.ItemStack
import net.minestom.server.utils.entity.EntityFinder
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.minestom.actor.MinestomCommandActor

@RegLamp
class GiveCommand {
    @LuckPermission("devoutserver.command.give")
    @Command("give")
    @Description("Usage: /give <target> <item> [<count>]")
    fun give(actor: MinestomCommandActor,target: EntityFinder,item: ItemStack,@Optional count: Int=1) {
        val actualCount = count.coerceAtMost(PlayerInventory.INVENTORY_SIZE * 64)
        var itemStack = item
        val itemStacks: List<ItemStack> = if (actualCount <= 64) {
            itemStack = itemStack.withAmount(actualCount)
            listOf(itemStack)
        } else {
            val stacks = mutableListOf<ItemStack>()
            var remainingCount = actualCount
            while (remainingCount > 64) {
                stacks.add(itemStack.withAmount(64))
                remainingCount -= 64
            }
            stacks.add(itemStack.withAmount(remainingCount))
            stacks
        }
        target.find(actor.sender()).forEach { entity ->
            if(entity is Player) {
                entity.inventory.addItemStacks(itemStacks, TransactionOption.ALL)
            }
        }
    }
}
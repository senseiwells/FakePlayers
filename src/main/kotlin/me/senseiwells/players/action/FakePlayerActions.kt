package me.senseiwells.players.action

import me.senseiwells.players.FakePlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.sqrt

// Basically a copy of MultiPlayerGameMode to be run server-side
class FakePlayerActions(
    private val player: FakePlayer
) {
    private val actions = ArrayList<FakePlayerAction>()
    private var action = 0

    private var destroyBlockPos = BlockPos(-1, -1, -1)
    private var destroyingItem = ItemStack.EMPTY
    private var destroyProgress = 0.0F
    private var destroyTicks = 0.0F
    private var destroyDelay = 0.0F
    private var isDestroying = false

    private var rightClickDelay = 0
    private var missTime = 0

    private lateinit var hitResult: HitResult

    var loop = false
    var paused = false

    var attacking: Boolean = false
    var attackingHeld: Boolean = false

    var using: Boolean = false
    var usingHeld: Boolean = false

    fun add(action: FakePlayerAction) {
        this.actions.add(action)
    }

    fun restart() {
        this.action = 0
    }

    fun clear() {
        this.actions.clear()
    }

    internal fun tick() {
        this.hitResult = this.getHitResult()

        if (this.rightClickDelay > 0) {
            this.rightClickDelay--
        }
        if (this.missTime > 0) {
            this.missTime--
        }

        if (!this.paused) {
            this.runActions()
        }

        var attacked = false
        if (this.player.isUsingItem) {
            if (!this.using) {
                this.releaseUsingItem()
            }
        } else {
            if (this.attacking) {
                attacked = this.startAttack()
            }
            if (this.using) {
                this.startUseItem()
            }
        }

        // Consume actions
        this.attacking = false
        this.using = false

        if (this.usingHeld && this.rightClickDelay == 0 && !this.player.isUsingItem) {
            this.startUseItem()
        }

        this.continueAttack(!attacked && this.attackingHeld)
    }

    private fun runActions() {
        if (this.loop && this.action >= this.actions.size) {
            this.action = 0
        }
        val start = this.action
        while (true) {
            val action = this.actions.getOrNull(this.action) ?: break
            if (!action.run(this.player)) {
                break
            }
            this.action += 1
            if (this.action >= this.actions.size) {
                if (!this.loop) {
                    break
                }
                this.action = 0
            }
            if (this.action == start) {
                break
            }
        }
    }

    private fun startAttack(): Boolean {
        if (this.missTime > 0) {
            return false
        }
        when (val hitResult = this.hitResult) {
            is EntityHitResult -> {
                val target = hitResult.entity
                this.handle(ServerboundInteractPacket.createAttackPacket(target, this.player.isShiftKeyDown))
            }
            is BlockHitResult -> {
                val pos = hitResult.blockPos
                if (!this.player.level().getBlockState(pos).isAir) {
                    this.startDestroyBlock(pos, hitResult.direction)
                    if (this.player.level().getBlockState(pos).isAir) {
                        return true
                    }
                }
            }
            else -> {
                if (!this.player.isCreative) {
                    this.missTime = 10
                }
            }
        }
        this.player.swing(InteractionHand.MAIN_HAND)
        return false
    }

    private fun continueAttack(isLeftClick: Boolean) {
        if (!isLeftClick) {
            this.missTime = 0
        }
        if (this.missTime > 0 || this.player.isUsingItem) {
            return
        }
        val hitResult = this.hitResult
        if (isLeftClick && hitResult is BlockHitResult) {
            val blockPos = hitResult.blockPos
            if (!this.player.level().getBlockState(blockPos).isAir) {
                if (this.continueDestroyBlock(blockPos, hitResult.direction)) {
                    this.player.swing(InteractionHand.MAIN_HAND)
                }
            }
        } else {
            this.stopDestroyBlock()
        }
    }

    private fun startUseItem() {
        if (this.isDestroying) {
            return
        }

        this.rightClickDelay = 4
        for (hand in InteractionHand.entries) {
            val stack = this.player.getItemInHand(hand)
            when (val hitResult = this.hitResult) {
                is EntityHitResult -> {
                    var result = this.interactAt(hitResult.entity, hitResult, hand)
                    if (!result.consumesAction()) {
                        result = this.interact(hitResult.entity, hand)
                    }
                    if (result.consumesAction()) {
                        if (result.shouldSwing()) {
                            this.player.swing(hand)
                        }
                        return
                    }
                }
                is BlockHitResult ->  {
                    val result = this.useItemOn(hand, hitResult)
                    if (result.consumesAction() || result == InteractionResult.FAIL) {
                        if (result.shouldSwing()) {
                            this.player.swing(hand)
                        }
                        return
                    }
                }
                else -> {
                    if (!stack.isEmpty) {
                        val result = this.useItem(hand)
                        if (result.consumesAction()) {
                            if (result.shouldSwing()) {
                                this.player.swing(hand)
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    private fun releaseUsingItem() {
        this.handle(ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN))
    }

    private fun startDestroyBlock(pos: BlockPos, face: Direction): Boolean {
        if (this.player.blockActionRestricted(this.player.level(), pos, this.player.gameMode.gameModeForPlayer)) {
            return false
        }
        if (!this.player.level().worldBorder.isWithinBounds(pos)) {
            return false
        }

        if (this.player.isCreative) {
            this.handle(ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, face))
            this.destroyDelay = 5.0F
            return true
        }
        if (!this.isDestroying || !this.sameDestroyTarget(pos)) {
            if (this.isDestroying) {
                this.handle(ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, face))
            }
            val state = this.player.level().getBlockState(pos)
            if (state.isAir || state.getDestroyProgress(this.player, this.player.level(), pos) < 1.0F) {
                this.isDestroying = true
                this.destroyBlockPos = pos
                this.destroyingItem = this.player.mainHandItem
                this.destroyProgress = 0.0F
                this.destroyTicks = 0.0F
            }

            this.handle(ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, face))
        }
        return true
    }

    private fun continueDestroyBlock(pos: BlockPos, face: Direction): Boolean {
        if (this.destroyDelay > 0) {
            this.destroyDelay--
            return true
        }
        if (this.player.isCreative && this.player.level().worldBorder.isWithinBounds(pos)) {
            this.handle(ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, face))
            this.destroyDelay = 5.0F
            return true
        }
        if (!this.sameDestroyTarget(pos)) {
            return this.startDestroyBlock(pos, face)
        }
        val state = this.player.level().getBlockState(pos)
        if (state.isAir) {
            this.isDestroying = false
            return false
        }
        this.destroyProgress += state.getDestroyProgress(this.player, this.player.level(), pos)
        this.destroyTicks++
        if (this.destroyProgress >= 1.0F) {
            this.isDestroying = false
            this.handle(ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, face))
            this.destroyProgress = 0.0F
            this.destroyTicks = 0.0F
            this.destroyDelay = 5.0F
        }
        return true
    }

    private fun stopDestroyBlock() {
        if (this.isDestroying) {
            this.handle(ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN))
            this.isDestroying = false
            this.destroyProgress = 0.0f
        }
    }

    private fun interactAt(target: Entity, hitResult: EntityHitResult, hand: InteractionHand): InteractionResult {
        val delta = hitResult.location.subtract(target.x, target.y, target.z)
        this.handle(ServerboundInteractPacket.createInteractionPacket(target, this.player.isShiftKeyDown, hand, delta))
        return this.player.connection().popResult(InteractionResult.FAIL)
    }

    private fun interact(target: Entity, hand: InteractionHand): InteractionResult {
        this.handle(ServerboundInteractPacket.createInteractionPacket(target, this.player.isShiftKeyDown, hand))
        return this.player.connection().popResult(InteractionResult.FAIL)
    }

    private fun useItemOn(hand: InteractionHand, result: BlockHitResult): InteractionResult {
        if (!this.player.level().worldBorder.isWithinBounds(result.blockPos)) {
            return InteractionResult.FAIL
        }
        this.handle(ServerboundUseItemOnPacket(hand, result, 0))
        return this.player.connection().popResult(InteractionResult.PASS)
    }

    private fun useItem(hand: InteractionHand): InteractionResult {
        this.handle(ServerboundUseItemPacket(hand, 0, this.player.yRot, this.player.xRot))
        return this.player.connection().popResult(InteractionResult.FAIL)
    }

    private fun getHitResult(): HitResult {
        val blockRange = this.player.blockInteractionRange()
        val entityRange = this.player.entityInteractionRange()
        var range = max(blockRange, entityRange)
        var rangeSqr = range * range
        val hitResult = this.player.pick(range, 1.0F, false)
        val eyes = this.player.eyePosition
        val distanceSqr = hitResult.location.distanceToSqr(eyes)

        if (hitResult.type != HitResult.Type.MISS) {
            rangeSqr = distanceSqr
            range = sqrt(distanceSqr)
        }

        val view = this.player.lookAngle
        val position = eyes.add(view.x * range, view.y * range, view.z * range)

        val box = this.player.boundingBox.expandTowards(view.scale(range)).inflate(1.0)
        val predicate = { entity: Entity -> !entity.isSpectator && entity.isPickable }
        val entityHitResult = ProjectileUtil.getEntityHitResult(this.player, eyes, position, box, predicate, rangeSqr)
        if (entityHitResult != null && entityHitResult.location.distanceToSqr(eyes) < distanceSqr) {
            return this.filterHitResult(entityHitResult, eyes, entityRange)
        }
        return this.filterHitResult(hitResult, eyes, blockRange)
    }

    private fun filterHitResult(hitResult: HitResult, pos: Vec3, range: Double): HitResult {
        val vec3 = hitResult.location
        if (!vec3.closerThan(pos, range)) {
            val vec32 = hitResult.location
            val direction = Direction.getNearest(vec32.x - pos.x, vec32.y - pos.y, vec32.z - pos.z)
            return BlockHitResult.miss(vec32, direction, BlockPos.containing(vec32))
        } else {
            return hitResult
        }
    }

    private fun sameDestroyTarget(pos: BlockPos): Boolean {
        val destroyingItem = this.player.mainHandItem
        return pos == this.destroyBlockPos && ItemStack.isSameItemSameComponents(destroyingItem, this.destroyingItem)
    }

    private fun <T: Packet<ServerGamePacketListener>> handle(packet: T) {
        packet.handle(this.player.connection)
    }

    internal fun serialize(): CompoundTag {
        val compound = CompoundTag()
        compound.putBoolean("attacking", this.attacking)
        compound.putBoolean("using", this.using)
        compound.putBoolean("attacking_held", this.attackingHeld)
        compound.putBoolean("using_held", this.usingHeld)
        compound.putBoolean("loop", this.loop)
        compound.putInt("action", this.action)
        val result = FakePlayerAction.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.actions).result()
        if (result.isPresent) {
            compound.put("actions", result.get())
        }
        return compound
    }

    internal fun deserialize(compound: CompoundTag) {
        this.attacking = compound.getBoolean("attacking")
        this.using = compound.getBoolean("using")
        this.attackingHeld = compound.getBoolean("attacking_held")
        this.usingHeld = compound.getBoolean("using_held")
        this.loop = compound.getBoolean("loop")
        this.action = compound.getInt("action")
        val result = FakePlayerAction.CODEC.listOf().parse(NbtOps.INSTANCE, compound.get("actions")).result()
        if (result.isPresent) {
            this.actions.clear()
            this.actions.addAll(result.get())
        }
    }
}
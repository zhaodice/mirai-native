/*
 *
 * Mirai Native
 *
 * Copyright (C) 2020 iTX Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author PeratX
 * @website https://github.com/iTXTech/mirai-native
 *
 */

package org.itxtech.mirainative.manager

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.TempMessageEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.recall
import net.mamoe.mirai.message.data.source
import org.itxtech.mirainative.MiraiNative

object CacheManager {
    private val msgCache = hashMapOf<Int, MessageSource>()
    private val evCache = hashMapOf<Int, BotEvent>()
    private val senders = hashMapOf<Long, Member>()
    private val internalId = atomic(0)

    fun nextId() = internalId.getAndIncrement()

    fun cacheEvent(event: BotEvent, id: Int = nextId()) = id.apply { evCache[this] = event }.toString()

    fun getEvent(id: String) = evCache[id.toInt()]?.also { evCache.remove(id.toInt()) }

    fun cacheMessage(message: MessageSource, id: Int = nextId()) = id.apply { msgCache[this] = message }

    fun cacheTempMessage(message: TempMessageEvent, id: Int = nextId()): Int {
        senders[message.sender.id] = message.sender
        return cacheMessage(message.message.source, id)
    }

    fun recall(id: Int): Boolean {
        val message = msgCache[id] ?: return false
        msgCache.remove(id)
        MiraiNative.launch {
            message.recall()
        }
        return true
    }

    fun getMessage(id: Int) = msgCache[id]

    fun findMember(id: Long) = senders[id]

    fun clear() {
        msgCache.clear()
        evCache.clear()
        senders.clear()
    }
}

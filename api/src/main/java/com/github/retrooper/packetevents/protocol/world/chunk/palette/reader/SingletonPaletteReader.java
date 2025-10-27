/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.world.chunk.palette.reader;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.Palette;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

/**
 * Added with 1.18
 * <p>
 * A palette containing one state.
 * Credit to MCProtocolLib
 */
public class SingletonPaletteReader implements Palette {

    protected final int state;

    @Deprecated
    public SingletonPaletteReader(NetStreamInput in) {
        this(in.readVarInt2Bytes());
    }

    public SingletonPaletteReader(PacketWrapper<?> wrapper) {
        this(wrapper.readVarInt());
    }

    public SingletonPaletteReader(int state) {
        this.state = state;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int idToState(int id) {
        if (id == 0) {
            return this.state;
        }
        return 0;
    }

    @Override
    public int getBits() {
        return 0;
    }
}

package com.crepetete.steamachievements.util.palette

import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.util.Util

/**
 * Created at 20 January, 2019.
 */
class PaletteBitmapResource(
    private val paletteBitmap: PaletteBitmap,
    private val bitmapPool: BitmapPool) : Resource<PaletteBitmap> {
    override fun get(): PaletteBitmap {
        return paletteBitmap
    }

    override fun getResourceClass(): Class<PaletteBitmap> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSize(): Int {
        return Util.getBitmapByteSize(paletteBitmap.bitmap)
    }

    override fun recycle() {
        paletteBitmap.bitmap.recycle()
    }
}
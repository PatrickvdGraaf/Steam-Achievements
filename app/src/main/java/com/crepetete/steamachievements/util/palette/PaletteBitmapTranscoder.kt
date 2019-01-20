package com.crepetete.steamachievements.util.palette

import android.content.Context
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder

/**
 * Created at 20 January, 2019.
 */
class PaletteBitmapTranscoder(
    context: Context,
    private val bitmapPool: BitmapPool = Glide.get(context).bitmapPool
) : ResourceTranscoder<Bitmap, PaletteBitmap> {
    override fun transcode(toTranscode: Resource<Bitmap>, options: Options): Resource<PaletteBitmap>? {
        val bitmap = toTranscode.get()
        val palette = Palette.Builder(bitmap).generate()
        val result = PaletteBitmap(bitmap, palette)
        return PaletteBitmapResource(result, bitmapPool)
    }
}
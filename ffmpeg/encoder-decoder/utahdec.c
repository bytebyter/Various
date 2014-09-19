#include "avcodec.h"
#include "bytestream.h"
#include "internal.h"

typedef struct UTAHContext {
} UTAHContext;

static int decode_frame(AVCodecContext *avctx, void *data, int *got_frame, AVPacket *avpkt){
	int ret; int skipSize;int fseek=8; int i=0;int j=0;	/*Return for buffer allocation,Number of dummy bytes per line,starting file seek position,output buffer index, input buffer index.*/
	const uint8_t *buf = avpkt->data;					/*Hold a pointer to the input buffer*/
	AVFrame *pict=data;									/*Hold a pointer to the output buffer*/
	
	/*Set the output pixel format to RGB8*/
	avctx->pix_fmt = AV_PIX_FMT_RGB8;
	bytestream_get_le32(&buf);
	/*Get the width and height*/
	avctx->width=bytestream_get_le16(&buf);
	avctx->height=bytestream_get_le16(&buf);
	
	/*Release the old buffer*/
	if (pict->data[0]) avctx->release_buffer(avctx, pict);

	/*Aquire a new buffer*/
	if (ret=ff_get_buffer(avctx, pict) < 0) { /*Attempt to aquire a large enough data buffer to hold our decompressed picture*/
                    av_log(avctx, AV_LOG_ERROR, "get_buffer() failed\n");
                    return ret;
    }
	
	skipSize=pict->linesize[0] - avctx->width;			/*Number of dummy bytes to skip per line*/
	for(int y=0;y<avctx->height;y++){
		for(int x=0;x<avctx->width;x++){
			pict->data[0][i]=avpkt->data[fseek+j];		/*Place the data from the input buffer into the output buffer*/
			j++;										/*Keeps track of the input buffer index*/
			i++;										/*Keeps track of the output buffer index*/
		}
		i+=skipSize;
	}
	
	
	/*Inform ffmpeg the output is a key frame and that it is ready for external usage*/
	pict->pict_type        = AV_PICTURE_TYPE_I;
    pict->key_frame        = 1;
	*got_frame=1;
	return avpkt->size;
}
						
static av_cold int utah_init(AVCodecContext *avctx)
{
    return 0;
}

static av_cold int utah_end(AVCodecContext *avctx)
{
	return 0;
}

AVCodec ff_utah_decoder = {
    .name           = "utah",
    .type           = AVMEDIA_TYPE_VIDEO,
    .id             = AV_CODEC_ID_UTAH,
    .priv_data_size = sizeof(UTAHContext),
    .init           = utah_init,
    .close          = utah_end,
    .decode         = decode_frame,
    .capabilities   = CODEC_CAP_DR1,
    .long_name      = NULL_IF_CONFIG_SMALL("utah image"),
};
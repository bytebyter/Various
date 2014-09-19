#include "libavutil/imgutils.h"
#include "libavutil/log.h"
#include "libavutil/opt.h"
#include "libavutil/pixdesc.h"

#include "libswscale/swscale.h"
#include "avcodec.h"
#include "config.h"
#include "bytestream.h"
#include "internal.h"

typedef struct UTAHContext {
    AVFrame picture;
} UTAHContext;

static av_cold int encode_init(AVCodecContext *avctx)
{
	UTAHContext *s = avctx->priv_data;
    avcodec_get_frame_defaults(&s->picture);	/*Empty the buffer*/
    avctx->coded_frame = &s->picture;			
	return 0;
}
static int encode_frame(AVCodecContext * avctx, AVPacket *pkt, const AVFrame *pict, int *got_packet){
		int compPos=0; int ret; int compSkip;	/*Compressed Index Position, Return for ff_alloc_packet2, How many bytes to skip per line.*/
		uint8_t *buf;							/*Output data Buffer*/
		/*Allocate a output buffer that is the size of the header + the size of the image data. If unsucessful exit this method.*/
		if ((ret = ff_alloc_packet2(avctx, pkt, 8+(avctx->width * avctx->height))) < 0)
			return ret;
		buf = pkt->data;						/*Pointer to the output data buffer*/
		/*Write UTAH header to the data stream.*/
		bytestream_put_byte(&buf, 'U');			
		bytestream_put_byte(&buf, 'T');
		bytestream_put_byte(&buf, 'A');
		bytestream_put_byte(&buf, 'H');
		/*Write the width and the height to the data stream as a 16bit unsigned integer*/
		bytestream_put_le16(&buf, avctx->width);                
		bytestream_put_le16(&buf, avctx->height);
		compSkip=pict->linesize[0] - (avctx->width);					/*Calculate the numer of bytes to skip per line*/
		for (int y=0;y<avctx->height;y++){
			for (int x=0;x<avctx->width;x++){
				bytestream_put_byte(&buf, pict->data[0][compPos++]);	/*Copy the input data stream into the output file buffer.*/
			}
			compPos+=compSkip;											/*Skip the dummy bytes*/
		}
		
		 pkt->flags |= AV_PKT_FLAG_KEY;									/*Set the output flag to that of a keyframe*/
		*got_packet = 1;												/*Set the got packet flag to true*/
		return 0;														/*Exit the function.*/
}

AVCodec ff_utah_encoder = {
    .name           = "utah",
    .type           = AVMEDIA_TYPE_VIDEO,
    .id             = AV_CODEC_ID_UTAH,
    .priv_data_size = sizeof(UTAHContext),
    .init           = encode_init,
    .encode2        = encode_frame,
	.capabilities   = CODEC_CAP_INTRA_ONLY,
    .pix_fmts       = (const enum AVPixelFormat[]){
		AV_PIX_FMT_RGB8, AV_PIX_FMT_NONE  
    },
    .long_name      = NULL_IF_CONFIG_SMALL("UTAH (Built for CS 3505 in U of U) image")
};
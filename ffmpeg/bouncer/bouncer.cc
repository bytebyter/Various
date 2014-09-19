#include <fstream>
#include <iostream>
#include <string>
#include <sstream>

#ifndef INT64_C
#define INT64_C(c) (c ## LL)
#define UINT64_C(c) (c ## ULL)
#endif

extern "C"
{
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavcodec/avcodec.h>
}

using namespace std;
void encode_Frame(AVFrame * in,const char * filename){
	AVCodec *codec; AVCodecContext *c= NULL;
    int i, ret, x, y, got_output;
    FILE *f; AVPacket pkt;
   
    // find the utah encoder
    codec = avcodec_find_encoder(AV_CODEC_ID_UTAH);
    if (!codec) {
        fprintf(stderr, "Codec not found\n");
        exit(1);
    }
	//get the encoders context.
    c = avcodec_alloc_context3(codec);
    if (!c) {
        fprintf(stderr, "Could not allocate video codec context\n");
        exit(1);
    }
	//set the outputs width height and color format.
    c->width = in->width;
    c->height = in->height;
    c->pix_fmt = codec->pix_fmts[0];
    if (avcodec_open2(c, codec, NULL) < 0) {
        fprintf(stderr, "Could not open codec\n");
        exit(1);
    }
	
	//Attempt to open the output file.
	f = fopen(filename, "wb");
    if (!f) {
        fprintf(stderr, "Could not open %s\n", filename);
        exit(1);
	}
	
	// Allocate an AVFrame structure
	AVFrame * conv=avcodec_alloc_frame();
	if(conv==NULL) return;
  
	//Force convert the color scheme to the encoders pixel format.
	struct SwsContext      *sws_ctx = NULL;
	int numBytes=avpicture_get_size(c->pix_fmt, c->width, c->height);
	uint8_t * buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
	sws_ctx = sws_getContext( c->width, c->height, (AVPixelFormat)in->format, c->width, c->height, c->pix_fmt, SWS_BILINEAR, NULL, NULL, NULL);
	avpicture_fill((AVPicture *)conv, buffer, c->pix_fmt, c->width, c->height);
	conv->width=c->width;
	conv->height=c->height;
	conv->format=c->pix_fmt;
	sws_scale( sws_ctx,(uint8_t const * const *)in->data,in->linesize,0, c->height, conv->data, conv->linesize);
		
	av_init_packet(&pkt);	//Initialize the output packet.
	pkt.data = NULL;    	// packet data will be allocated by the encoder
	pkt.size = 0;			//...

	//Encode the input frame.
	ret = avcodec_encode_video2(c, &pkt, conv, &got_output);
	if (ret < 0) {
		fprintf(stderr, "Error encoding frame\n");
		exit(1);
	}
    if (got_output) {
            printf("Write frame %3d (size=%5d)\n", i, pkt.size);
            fwrite(pkt.data, 1, pkt.size, f);
            av_free_packet(&pkt);
    }
	fclose(f);

    avcodec_close(c);
    av_free(c);
	av_free(conv);
}

AVFrame	* Load_AVFrame(const char * file,AVPixelFormat format,int * Width, int * Height){
	AVFormatContext *pFormatCtx = NULL;
	int             i, videoStream;
	AVCodecContext  *pCodecCtx = NULL;
	AVCodec         *pCodec = NULL;
	AVFrame         *pFrame = NULL; 
	AVFrame         *pFrameRGB = NULL;
	AVPacket        packet;
	int             frameFinished;
	int             numBytes;
	uint8_t         *buffer = NULL;
	AVDictionary    *optionsDict = NULL;
	struct SwsContext      *sws_ctx = NULL;
  
	// Register all formats and codecs
	av_register_all();
  
	// Open video file
	if(avformat_open_input(&pFormatCtx, file, NULL, NULL)!=0) return NULL; // Couldn't open file
  
	// Retrieve stream information
	if(avformat_find_stream_info(pFormatCtx, NULL)<0) return NULL; // Couldn't find stream information
  
	// Dump information about file onto standard error
	av_dump_format(pFormatCtx, 0, file, 0);
  
	// Find the first video stream
	videoStream=-1;
	for(i=0; i<pFormatCtx->nb_streams; i++)
		if(pFormatCtx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO) {
		videoStream=i;
		break;
    }
	if(videoStream==-1) return NULL; // Didn't find a video stream
  
	// Get a pointer to the codec context for the video stream
	pCodecCtx=pFormatCtx->streams[videoStream]->codec;
  
	// Find the decoder for the video stream
	pCodec=avcodec_find_decoder(pCodecCtx->codec_id);
	if(pCodec==NULL) {
		fprintf(stderr, "Unsupported codec!\n");
		return NULL; // Codec not found
	}
  
	// Open codec
	if(avcodec_open2(pCodecCtx, pCodec, &optionsDict)<0) return NULL; // Could not open codec
  
	// Allocate video frame
	pFrame=avcodec_alloc_frame();
  
	// Allocate an AVFrame structure
	pFrameRGB=avcodec_alloc_frame();
	if(pFrameRGB==NULL) return NULL;
  
	int width=(Width!=NULL)? *Width : pCodecCtx->width;
	int height=(Height!=NULL)? *Height :pCodecCtx->height;
	// Determine required buffer size and allocate buffer
	numBytes=avpicture_get_size(format, width, height);
	buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
	sws_ctx =
		sws_getContext
		(
			pCodecCtx->width,
			pCodecCtx->height,
			pCodecCtx->pix_fmt,
			width,
			height,
			format,
			SWS_BILINEAR,
			NULL,
			NULL,
			NULL
		);
  
	// Assign appropriate parts of buffer to image planes in pFrameRGB
	// Note that pFrameRGB is an AVFrame, but AVFrame is a superset
	// of AVPicture
	avpicture_fill((AVPicture *)pFrameRGB, buffer, format, width, height);
	pFrameRGB->width=width;
	pFrameRGB->height=height;
	pFrameRGB->format=format;
  
  
	// Read frames and save first five frames to disk
	i=0; bool gotFrame=false;
	while(av_read_frame(pFormatCtx, &packet)>=0) {
		// Is this a packet from the video stream?
		if(packet.stream_index==videoStream) {
		// Decode video frame
		avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);
      
		// Did we get a video frame?
		if(frameFinished) {
		// Convert the image from its native format to RGB
			sws_scale
			(
				sws_ctx,
				(uint8_t const * const *)pFrame->data,
				pFrame->linesize,
				0,
				pCodecCtx->height,
				pFrameRGB->data,
				pFrameRGB->linesize
			);
			// Save the frame to disk
			gotFrame=true;
		}   
		// Free the packet that was allocated by av_read_frame
		av_free_packet(&packet);
		if (gotFrame) break;
	
		}
	}
 
	// Free the YUV frame
	av_free(pFrame);
	// Close the codec
	avcodec_close(pCodecCtx);  
	// Close the video file
	avformat_close_input(&pFormatCtx);
  
	return  pFrameRGB;
}



//Makes 300 frames worth of ball bouncing goodness.
bool makeAnim(string & fBackground){
	int bLeft,bTop;										//Position of ball images left/upper edges. 
	int maxTop;											//Max height ball can bounce to.
	int Accel;											//Accelleration rate in pixels.
	int count;											//Used to adjust balls direction.
	int direction;										//Balls direction. 0-down 1-up.
	int adjustedSize;									//Adjusted width and height to scale the ball/circle too.
	string fBall="Ball.png";							//Ball image filename
	
	ifstream in(fBackground.c_str());					//File input stream used to check if the file exists.
	if(in.is_open()) in.close();						//Check to see if the background file exists if it does close it.
	else{												//Otherwise inform the user the file cannot be accessed and exit the function.
		cout<<"Background file access error!"<<endl;
		return false;
	}
	//Load the background and the ball.
	AVFrame * pBackground=Load_AVFrame(fBackground.c_str(),AV_PIX_FMT_RGB24,NULL,NULL );
	if (pBackground==NULL) return false;
	adjustedSize=(pBackground->height*0.15);
	if (adjustedSize<1) adjustedSize=1;		//If that happens to round down to 0. Set the ball size to 1x1.
	AVFrame * pBall= Load_AVFrame(fBall.c_str(),AV_PIX_FMT_RGB24,&adjustedSize, &adjustedSize);
	if (pBall==NULL) return false;
	
	//Setup initial ball position/direction/acceleration here.
	maxTop=0;											// Set the max positon of the ball.
	bLeft=(pBackground->width + pBall->width) / 2;		// Center the ball.
	bTop=maxTop;										// Place ball at top of picture.
	Accel=(pBackground->height - pBall->height)/ 15;	// Acceleration rate of the ball is relative to height
	count=0;											//Keeps track of how many frames have elapsed since a directional change has occured.
	direction=0;										//Ball is moving in a downward direction initially;
	
	//Create an output buffer in RGB24.
	AVFrame *frame;
	frame = avcodec_alloc_frame();
    if (!frame) {
        fprintf(stderr, "Could not allocate video frame\n");
        exit(1);
    }
    frame->format = pBackground->format;
    frame->width  = pBackground->width;
    frame->height = pBackground->height;
	int numBytes=avpicture_get_size(AV_PIX_FMT_RGB24, pBackground->width, pBackground->height);
	uint8_t * buffer=(uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
    avpicture_fill((AVPicture *)frame, buffer, AV_PIX_FMT_RGB24, pBackground->width, pBackground->height);
	
	unsigned long bkSeek=0; unsigned long ballSeek=0;							//Set the initial seek position for the background and ball's image buffer.
	unsigned long bkSkip = pBackground->linesize[0] -(pBackground->width * 3);	//Calculate the number of dummy bytes that need to be skipped for the background
	unsigned long ballSkip=pBall->linesize[0] - (pBall->width * 3);				//Calculate the number of dummy bytes that need to be skipped for the ball.
	unsigned long outSkip = frame->linesize[0] -(frame->width * 3);				//Calculate the number of dummy bytes that need to be skipped for the background
	unsigned long outPos=0;
	
	//Create 300 images for an animation. (Limited to 1 currently.)
	for(int f=1;f<=300;f++){							//Generate 300 frames.
		//Copy each pixel from the background and the ball as necessary to create an output image.
		ballSeek=0;
		bkSeek=0;
		outPos=0;
		for(int y=0; y<pBackground->height;y++){
			for(int x=0;x<pBackground->width;x++){
				if (x>=bLeft && x<=bLeft+pBall->width && y>=bTop && y<=bTop+pBall->height){//Check to see if we need to copy pixel data from the ball.
					if(pBall->data[0][ballSeek]==255 && pBall->data[0][ballSeek+1]==0 && pBall->data[0][ballSeek+2]==0){ //Is the color white (Transparent Color)
						frame->data[0][outPos]=pBackground->data[0][bkSeek];	//If so copy the background image's pixel information to the output buffer. (Red)
						frame->data[0][outPos+1]=pBackground->data[0][bkSeek+1];//Green
						frame->data[0][outPos+2]=pBackground->data[0][bkSeek+2];//Blue
					}
					else{												//The balls current pixel color is not tranparent.
						frame->data[0][outPos]=pBall->data[0][ballSeek];		//Copy the balls pixel data to the output buffer. (Red)
						frame->data[0][outPos+1]=pBall->data[0][ballSeek+1];	//Green
						frame->data[0][outPos+2]=pBall->data[0][ballSeek+2];	//Blue
					}
					frame->data[0][outPos]=pBall->data[0][ballSeek];		//Copy the balls pixel data to the output buffer. (Red)
					frame->data[0][outPos+1]=pBall->data[0][ballSeek+1];	//Green
					frame->data[0][outPos+2]=pBall->data[0][ballSeek+2];	//Blue
					ballSeek+=3;										//3 bytes per pixel (R,G,B), therefore seek forward 3 bytes for next pixel.
				}
				else{													//The ball has not been encountered yet, therefore copy the background image's data to the output buffer.	
				
					frame->data[0][outPos]=pBackground->data[0][bkSeek];		//If so copy the background image's pixel information to the output buffer. (Red)
					frame->data[0][outPos+1]=pBackground->data[0][bkSeek+1];	//Green
					frame->data[0][outPos+2]=pBackground->data[0][bkSeek+2];	//Blue
				}
				bkSeek+=3;												//Update the background/output buffers seek position.
				outPos+=3;;
			}
			ballSeek+=ballSkip;											//Skip the ball images dummy bytes.
			bkSeek+=bkSkip;												//Skip the background images dummy bytes here.
			outPos+=outSkip;
			
		}
		stringstream ss;
		if (f<10) ss <<"00"<<f<<".utah";
		else if(f>=10 & f<100) ss <<"0"<<f<<".utah";
		else if (f>=100) ss<<f<<".utah";
		
		//Encode the frame and right it to a file.
		encode_Frame(frame,ss.str().c_str());
		
		if(direction==0) bTop+=Accel;														//Change the balls position based on direction
		else bTop-=Accel;																	//...
		if (bTop<0) bTop=0;																	//Enforce y boundaries.
		else if ( (bTop+pBall->height) > pBackground->height) bTop=pBackground->height-pBall->height;		//...
		
		count++;										//Update elapsed frame count;
		if (count==15){									//Time for a direction change.
			if(direction==0) direction=1;				//If the ball is moving up, change it to be moving in a downward direction.
			else direction=0;							//...upward direction.
			count=0;									//Reset the frame counter.
		}
	}
	//Free the background image buffers.
	av_free(pBackground);
	//Free the ball image buffers.
	av_free(pBall);
	av_free(frame);
	return true;										//300 Images were sucessfully created!
}


int main(int argc, char* argv[]){
	if (argc==2){
	string lStr (argv[1]);
			if (lStr.length()>4){
				if (lStr.substr(lStr.length()-4,4)==".jpg"){
					if(!makeAnim(lStr)) cout<<"Animation Creation Failed!"<<endl;
				}
				else cout<<"Input file is not supported"<<endl;
			}
			else cout<<"Input file is not supported."<<endl;
	}
	else cout<<"Missing arguements!"<<endl;
	return 0;
}
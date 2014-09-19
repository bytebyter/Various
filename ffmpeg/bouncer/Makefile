all: bouncer.cc
	 g++ -I ../ffmpeg/include/ -L../ffmpeg/lib bouncer.cc `pkg-config --cflags --libs libavutil libavformat libavcodec libswscale`
clean: 
		rm *.utah *.mp4 a.out
movie:
	../ffmpeg/bin/ffmpeg -f image2 -i %03d.utah -r 30 out.mp4

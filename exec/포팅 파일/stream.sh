#!/bin/bash
PEER_A=$1
PEER_V=$2
PEER_IP="172.26.2.248"
SELF_A=5006
SELF_ASSRC=445566
SELF_V=5004
SELF_VSSRC=112233
gst-launch-1.0 -e rtpbin name=r sdes="application/x-rtp-source-sdes,cname=(string)\"user\@example.com\"" uridecodebin uri="$(yt-dlp --extractor-args youtube:player_client=android --throttled-rate 100K --format "worst[ext=mp4][protocol=https]" --get-url https://www.youtube.com/watch?v=$3)" name=d d. ! queue ! audioconvert ! audioresample ! opusenc ! rtpopuspay ! "application/x-rtp,payload=(int)96,clock-rate=(int)48000,ssrc=(uint)445566" ! r.send_rtp_sink_0 d. ! queue ! videoconvert ! x264enc tune=zerolatency ! rtph264pay ! "application/x-rtp,payload=(int)103,clock-rate=(int)90000,ssrc=(uint)112233" ! r.send_rtp_sink_1 r.send_rtp_src_0 ! udpsink host=172.26.2.248 port=$1 bind-port=5006 r.send_rtcp_src_0 ! udpsink host=172.26.2.248 port=`expr $1 + 1` bind-port=5007 sync=false async=false udpsrc port=5007 ! r.recv_rtcp_sink_0 r.send_rtp_src_1 ! udpsink host=172.26.2.248 port=$2 bind-port=5004 r.send_rtcp_src_1 ! udpsink host=172.26.2.248 port=`expr $2 + 1` bind-port=5005 sync=false async=false udpsrc port=5005 ! tee name=t t. ! queue ! r.recv_rtcp_sink_1 t. ! queue ! fakesink dump=true async=false

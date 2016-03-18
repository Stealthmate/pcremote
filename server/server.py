import socket 
import struct
import signal
import sys
import math

import winaudio
import winio

host = '192.168.100.5' 
port = 25565
backlog = 5 
	
CONTROL_CODE_KILL       = 0x00
CONTROL_CODE_HEARTBEAT  = 0x01
CONTROL_CODE_CLOSE_CONNECTION = 0x0F

ACTION_CODE_VOL        = 0x11
ACTION_CODE_NEXT_TRACK = 0x12
ACTION_CODE_PREV_TRACK = 0x13
ACTION_CODE_PLAY_PAUSE = 0x14
ACTION_CODE_STOP       = 0x15

RETURN_CODE_HEARTBEAT = 0x00
RETURN_CODE_INVALID_COMMAND = 0x01
RETURN_CODE_VALUE_ERROR = 0x02
RETURN_CODE_CLOSE_CONNECTION = 0x0F


client = None
address = None

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
s.bind((host,port)) 
s.listen(backlog)
s.settimeout(0.1)

vol = winaudio.volume.GetMasterVolumeLevel()


def signal_handler(signal, frame):
	print("Closing")
	s.settimeout(0.1)
	if client is not None:
		client.close()
	s.close();
	sys.exit(0)
	
signal.signal(signal.SIGINT, signal_handler)

def getMaster():
	p = math.pow(10, (winaudio.volume.GetMasterVolumeLevel() + 40.0)/20.0)
	b = int(p).to_bytes(4, 'little')[:1]
	return b
	

def quit():
	client.close()
	s.close();
	sys.exit(0)
				
def adjustVolume(percent):
	if percent == 0:
		winaudio.volume.SetMasterVolumeLevel(-60.0, None)
		return
		
	vol = 20*math.log(percent/100, 10)
	winaudio.volume.SetMasterVolumeLevel(vol, None)
		
def nextTrack():
	winio.SendKey(winio.VK_MEDIA_NEXT_TRACK)
		
def prevTrack():
	winio.SendKey(winio.VK_MEDIA_PREV_TRACK)

def playPauseTrack():
	winio.SendKey(winio.VK_MEDIA_PLAY_PAUSE)
	
def stopTrack():
	winio.SendKey(winio.VK_MEDIA_STOP)
		

def packcmd(cmd):
	cmdbytes = cmd.to_bytes(4, 'little')
	return cmdbytes[:1]
		
while 1: 
	try:
		if client is None:
			client, address = s.accept()
			client.settimeout(10)
			print("Hello there, " + str(address[0]))
			client.send(getMaster())
			
			
		data = client.recv(2)
		if not data:
			print("Connection closed abruptly")
			client = None
			
		else:	
			while len(data) < 2:
				data = data + client.recv(2 - len(data))
			
			#print("Fulldata " + str(data))

			#Control requests
			if data[0] == CONTROL_CODE_KILL:
				#print("Kill")
				quit()
				
			elif data[0] == CONTROL_CODE_CLOSE_CONNECTION:
				#print("Closing connection")
				client.send(packcmd(RETURN_CODE_CLOSE_CONNECTION))
				client.close()
				client = None
				
			elif data[0] == CONTROL_CODE_HEARTBEAT:
				#print("Heartbeat")
				client.send(packcmd(RETURN_CODE_HEARTBEAT))
			
			#Action requests
			elif data[0] == ACTION_CODE_VOL:
				adjustVolume(data[1])
			elif data[0] == ACTION_CODE_NEXT_TRACK:
				nextTrack()
			elif data[0] == ACTION_CODE_PREV_TRACK:
				prevTrack()
			elif data[0] == ACTION_CODE_PLAY_PAUSE:
				playPauseTrack()
			elif data[0] == ACTION_CODE_STOP:
				stopTrack()
			else:
				print("I'm receiving random data")
			
	except ValueError:
			print("Value Error:")
			print(data)
			
	except socket.timeout:
			if client is not None:
				print("Goodbye " + str(address[0]) + " :(")
				client.send(packcmd(RETURN_CODE_CLOSE_CONNECTION))
			client = None
		
	

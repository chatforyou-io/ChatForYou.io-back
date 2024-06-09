package com.chatforyou.io.client;

/**
 * See
 * {@link Session#createConnection(ConnectionProperties)}
 */
public enum ConnectionType {

	/**
	 * WebRTC connection. This is the normal type of Connection for a regular user
	 * connecting to a session from an application.
	 */
	WEBRTC,

	/**
	 * IP camera connection. This is the type of Connection used by IP cameras to
	 * connect to a session.
	 */
	IPCAM
}

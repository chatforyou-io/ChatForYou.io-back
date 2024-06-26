/*
 * (C) Copyright 2017-2022 OpenVidu (https://openvidu.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.chatforyou.io.client;

/**
 * Defines error responses from OpenVidu Server. See error codes at
 * <a href="/en/stable/reference-docs/REST-API">REST API docs</a>
 */
public class OpenViduHttpException extends OpenViduException {

	private static final long serialVersionUID = 1L;
	private int status;

	protected OpenViduHttpException(int status) {
		super(Integer.toString(status));
		this.status = status;
	}

	/**
	 * @return The unexpected status of the HTTP request. See error codes meaning at
	 *         <a href="/en/stable/reference-docs/REST-API">REST API docs</a>
	 */
	public int getStatus() {
		return this.status;
	}

}

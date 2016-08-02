/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.skullclient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

public class SkullClient {

	static final String apiFormat = "https://skulls.inventivetalent.org/api/?url=%s";

	static final Executor requestExecutor = Executors.newSingleThreadExecutor();

	static long nextRequest = 0;

	/**
	 * Creates skull data from the image URL
	 *
	 * @param url      url to load image from
	 * @param callback {@link SkullCallback} to inform about progress
	 */
	public static void create(final URL url, final SkullCallback callback) {
		create(url, false, callback);
	}

	/**
	 * Creates skull data from the image URL
	 *
	 * @param url           url to load image from
	 * @param privateUpload whether to upload a private skull (won't be visible publicly)
	 * @param callback      {@link SkullCallback} to inform about progress
	 */
	public static void create(final URL url, final boolean privateUpload, final SkullCallback callback) {
		checkNotNull(url);
		checkNotNull(callback);
		requestExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (System.currentTimeMillis() < nextRequest) {
						long delay = (nextRequest - System.currentTimeMillis());
						callback.waiting(delay);
						Thread.sleep(delay + 100);
					}
					callback.uploading();

					Connection connection = Jsoup.connect(String.format(apiFormat, url.toString())).userAgent("CustomSkullClient").timeout(10000).ignoreContentType(true).ignoreHttpErrors(true);
					if (privateUpload) {
						connection = connection.header("X-Skull-Private", "true");
					}
					String body = connection.execute().body();
					try {
						JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
						if (jsonObject.has("error")) {
							callback.error(jsonObject.get("error").getAsString());
							return;
						}

						nextRequest = System.currentTimeMillis() + (jsonObject.get("nextRequest").getAsInt() * 1000);
						callback.done(SkullData.from(jsonObject));
					} catch (JsonParseException e) {
						System.err.println(body);
						throw new RuntimeException("Received malformed Json", e);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * Creates skull data from the image URL
	 *
	 * @param url      url to load image from
	 * @param callback {@link SkullCallback} to inform about progress
	 * @see #create(URL, SkullCallback)
	 */
	public static void create(String url, SkullCallback callback) {
		try {
			create(new URL(checkNotNull(url)), checkNotNull(callback));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}

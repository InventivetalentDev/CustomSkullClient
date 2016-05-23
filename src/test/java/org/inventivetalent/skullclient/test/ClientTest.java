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

package org.inventivetalent.skullclient.test;

import org.inventivetalent.skullclient.SkullCallback;
import org.inventivetalent.skullclient.SkullClient;
import org.inventivetalent.skullclient.SkullData;
import org.inventivetalent.skullclient.SkullProperty;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ClientTest {

	@Test
	public void validateTest() throws Exception {
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		final SkullData[] loadedData = new SkullData[1];
		final String[] loadError = new String[1];

		String url = "https://i.imgur.com/g9zt2W0.png";
		System.out.println("Testing URL " + url);
		SkullClient.create(url, new SkullCallback() {
			@Override
			public void waiting(long delay) {
				System.out.println("[waiting]");
			}

			@Override
			public void uploading() {
				System.out.println("[uploading]");
			}

			@Override
			public void error(String error) {
				System.out.println("[error]");

				loadError[0] = error;
				countDownLatch.countDown();
			}

			@Override
			public void done(SkullData data) {
				System.out.println("[done]");

				loadedData[0] = data;
				countDownLatch.countDown();
			}
		});

		countDownLatch.await(60, TimeUnit.SECONDS);

		assertNull(loadError[0]);

		SkullData data = loadedData[0];
		assertNotNull(data);
		assertNotNull(data.getProperties());
		assertNotNull(data.getProperties().getTextures());
		assertTrue(data.getProperties().getTextures().length == 1);
		assertNotNull(data.getProperties().firstTexture());

		SkullProperty texture = data.getProperties().firstTexture();
		assertNotNull(texture.getName());
		assertNotNull(texture.getValue());

		System.out.println("Texture: ");
		System.out.println(data.getProperties().firstTexture());
	}

}

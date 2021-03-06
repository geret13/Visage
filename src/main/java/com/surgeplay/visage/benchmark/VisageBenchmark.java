/*
 * Visage
 * Copyright (c) 2015-2016, Aesen Vismea <aesen@unascribed.com>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.surgeplay.visage.benchmark;

import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.surgeplay.visage.Visage;
import com.surgeplay.visage.VisageRunner;
import com.surgeplay.visage.slave.render.FullRenderer;
import com.surgeplay.visage.slave.render.Renderer;

public class VisageBenchmark extends Thread implements VisageRunner {
	private static final long TARGET = 5000000000L;
	private int num = 1;
	private NumberFormat format = NumberFormat.getInstance();
	private int total = 0;
	private BufferedImage skin;
	public VisageBenchmark() {
		super("Benchmark thread");
	}
	@Override
	public void run() {
		Renderer renderer = new FullRenderer();
		try {
			Visage.log.warning("VISAGE IS NOT AN ACCURATE HARDWARE BENCHMARK. This benchmark is to give you an idea of how well this machine would work as a Visage slave.");
			Visage.log.info("Loading skin...");
			float score = 0;
			skin = ImageIO.read(ClassLoader.getSystemResource("test_skin.png"));
			
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
			
			score += bench(1, 512, true) / 4f;
			score += bench(1, 128, true) / 5f;
			score += bench(1,  32, true) / 6f;
			
			score += bench(2, 512, true) / 3f;
			score += bench(2, 128, true) / 4f;
			score += bench(2,  32, true) / 5f;
			
			score += bench(3, 512, true) / 2f;
			score += bench(3, 128, true) / 3f;
			score += bench(3,  32, true) / 4f;
			
			score += bench(4, 512, true) / 1f;
			score += bench(4, 128, true) / 2f;
			score += bench(4,  32, true) / 3f;
			
			
			score += bench(1, 512, false) / 12f;
			score += bench(1, 128, false) / 15f;
			score += bench(1,  32, false) / 18f;
			
			score += bench(2, 512, false) / 9f;
			score += bench(2, 128, false) / 12f;
			score += bench(2,  32, false) / 15f;
			
			score += bench(3, 512, false) / 6f;
			score += bench(3, 128, false) / 9f;
			score += bench(3,  32, false) / 12f;
			
			score += bench(4, 512, false) / 3f;
			score += bench(4, 128, false) / 6f;
			score += bench(4,  32, false) / 9f;
			
			renderer.destroy();
			Visage.log.info("Done. Performed "+total+" renders.");
			Visage.log.info("Your arbitrary number is "+(score / (num-1)));
		} catch (Exception e) {
			Visage.log.log(Level.SEVERE, "Unexpected error occured during benchmark", e);
		}
	}
	
	private int bench(int supersampling, int size, boolean readpixels) throws Exception {
		int csize = size*supersampling;
		Visage.log.info("--");
		Visage.log.info("Starting benchmark #"+(num++)+" - "+supersampling+"x supersampling, "+size+"x"+size+" result, "+csize+"x"+csize+" canvas, "+(readpixels ? "with" : "without")+" readpixels)...");
		try {
			Visage.log.info("Setting up renderer...");
			Renderer renderer = new FullRenderer();
			renderer.init(supersampling);
			renderer.setSkin(skin);
			int count = 0;
			long renderTime = 0;
			long readTime = 0;
			Visage.log.info("Taking out the trash...");
			System.gc();
			Visage.log.info("Rendering...");
			long start = System.nanoTime();
			while (System.nanoTime()-start < TARGET) {
				long renderStart = System.nanoTime();
				renderer.render(csize, csize);
				renderTime += System.nanoTime()-renderStart;
				if (readpixels) {
					long readStart = System.nanoTime();
					renderer.readPixels(csize, csize);
					readTime += System.nanoTime()-readStart;
				}
				count++;
			}
			long runtime = System.nanoTime()-start;
			Visage.log.info("Cleaning up...");
			renderer.destroy();
			System.gc();
			double runtimeS = (runtime/1000000000D);
			double renderTimeM = ((renderTime/(double)count)/1000000D);
			Visage.log.fine("Rendered "+count+" players in "+format.format(runtimeS)+" seconds");
			Visage.log.fine("Avg. "+format.format(renderTimeM)+" millis per render");
			if (readpixels) {
				double readTimeM = ((readTime/(double)count)/1000000D);
				Visage.log.fine("Avg. "+format.format(readTimeM)+" millis per readPixels");
			}
			total += count;
			return count;
		} catch (Exception e) {
			Visage.log.log(Level.SEVERE, "An unexpected error occurred", e);
			return 0;
		}
	}
	@Override
	public void shutdown() {
	}

}

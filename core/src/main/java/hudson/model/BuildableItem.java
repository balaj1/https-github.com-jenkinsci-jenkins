/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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

package hudson.model;

import hudson.model.Queue.Task;

/**
 * {@link Item} that can be "built", for
 * whatever meaning of "build".
 *
 * <p>
 * This interface is used by utility code.
 *
 * @author Kohsuke Kawaguchi
 */
public interface BuildableItem extends Item, Task {
    /**
     * @deprecated
     *    Use {@link #scheduleBuild(Cause)}.  Since 1.283
     */
    @Deprecated
    default boolean scheduleBuild() {
        return scheduleBuild(new Cause.LegacyCodeCause());
    }

    boolean scheduleBuild(Cause c);
    /**
     * @deprecated
     *    Use {@link #scheduleBuild(int, Cause)}.  Since 1.283
     */

    @Deprecated
    default boolean scheduleBuild(int quietPeriod) {
        return scheduleBuild(quietPeriod, new Cause.LegacyCodeCause());
    }

    boolean scheduleBuild(int quietPeriod, Cause c);
}

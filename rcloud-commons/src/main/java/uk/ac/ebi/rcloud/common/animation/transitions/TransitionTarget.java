/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ebi.rcloud.common.animation.transitions;

/**
 * This interface is implemented by the "target" of ScreenTransition.  This
 * target will be called by ScreenTransition during the transition setup
 * process, so that the application can modify the application 
 * state appropriately.
 * 
 * @see uk.ac.ebi.rcloud.common.animation.transitions.ScreenTransition#ScreenTransition(JComponent, uk.ac.ebi.rcloud.common.animation.transitions.TransitionTarget, int)
 * @see uk.ac.ebi.rcloud.common.animation.transitions.ScreenTransition#ScreenTransition(JComponent, uk.ac.ebi.rcloud.common.animation.transitions.TransitionTarget, Animator)
 * 
 * @author Chet Haase
 */
public interface TransitionTarget {
    
    /**
     * This method is called during the {@link uk.ac.ebi.rcloud.common.animation.transitions.ScreenTransition#start()} method.
     * <p>
     * Implementors will change the UI in their transition container in
     * this method. This tells ScreenTransition the end-state of the components
     * for the upcoming transition.  After this method is complete, 
     * <code>ScreenTransition</code> has the information it needs to run 
     * the transition and the animation will begin.
     */
    public void setupNextScreen();
    
}

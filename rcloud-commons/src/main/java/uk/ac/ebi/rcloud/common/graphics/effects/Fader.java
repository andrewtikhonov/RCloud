/*
 * R Cloud - R-based Cloud Platform for Computational Research
 * at EMBL-EBI (European Bioinformatics Institute)
 *
 * Copyright (C) 2007-2015 European Bioinformatics Institute
 * Copyright (C) 2009-2015 Andrew Tikhonov - andrew.tikhonov@gmail.com
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ebi.rcloud.common.graphics.effects;

import uk.ac.ebi.rcloud.common.animation.timing.Animator;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 16, 2009
 * Time: 1:05:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Fader {

    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;

    private Timer fadeInTimer;
    private Timer fadeOutTimer;

    private ActionListener fadeInAction;
    private ActionListener fadeOutAction;

    private final static int FADEDIN  = 0;
    private final static int FADEDOUT = 1;

    private int state = FADEDOUT;

    public Fader(Animator fadeIn, Animator fadeOut) {
        fadeInAnimator = fadeIn;
        fadeOutAnimator = fadeOut;

        fadeInAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (state == FADEDIN) return;

                        if (fadeOutAnimator.isRunning()){
                            fadeOutAnimator.stop();
                        }
                        if (!fadeInAnimator.isRunning()){
                            fadeInAnimator.start();

                            state = FADEDIN;
                        }
                    }
                });
            }
        };

        fadeOutAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (state == FADEDOUT) return;
                        
                        if (fadeInAnimator.isRunning()){
                            fadeInAnimator.stop();
                        }
                        if (!fadeOutAnimator.isRunning()){
                            fadeOutAnimator.start();

                            state = FADEDOUT;                            
                        }
                    }
                });
            }
        };

        fadeInTimer  = new Timer(0, fadeInAction);
        fadeInTimer.setRepeats(false);

        fadeOutTimer = new Timer(0, fadeOutAction);
        fadeOutTimer.setRepeats(false);
    }

    public void fadeIn(int delay) {
        fadeOutTimer.stop();
        fadeInTimer.stop();
        fadeInTimer.setInitialDelay(delay);
        fadeInTimer.start();
    }

    public void fadeOut(int delay) {
        fadeInTimer.stop();
        fadeOutTimer.stop();
        fadeOutTimer.setInitialDelay(delay);
        fadeOutTimer.start();
    }


}

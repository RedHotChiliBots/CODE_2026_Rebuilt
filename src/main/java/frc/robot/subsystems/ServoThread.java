// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.servohub.ServoChannel;
import frc.robot.subsystems.Climber.HookSP;

public class ServoThread implements Runnable {
    private final HookSP sp;
    private final ServoChannel servo;

    public ServoThread(ServoChannel servo, HookSP sp) {
        this.servo = servo;
        this.sp = sp;
    }

    @Override
    public void run() {
        System.out.println("Executing task with: " + " at " + System.currentTimeMillis());
    }
}
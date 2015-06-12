package ch.epfl.cs211.game;

import java.util.ArrayList;

import processing.core.PShape;

public class GLOBAL_VAR {

	//Window Size
	protected static final int width = 800;
	protected static final int height = 800;
	
	//Number of Frame per Seconds
	protected static final float fps          = 60.0f;

	//Multiplying factor for the velocity changes
	protected static final float timeFactor   = 1.0f / fps;

	//Gravitation constant
	protected static final float g            = 9.81f;

	//Elasticity used for bounces, 1 = no energy loss
	protected static final float elasticity   = 0.97f;

	//Box parameters
	protected static final float boxThickness = 20.0f;
	protected static final float boxWidth     = 400.0f;
	protected static final float boxHeight    = 400.0f;
	protected static final int   boxColor     = 0xffB404A9;

	//Sphere parameters
	protected static final float sphereRadius = 24.0f;
	protected static final float sphereOffset = -sphereRadius - boxThickness/2.0f;
	protected static final int   sphereColor  = 0xff1D10E0;

	//Cylinder parameters
	protected static final float cylinderBaseSize   = 50;
	protected static final float cylinderHeight     = 50;
	protected static final float cylinderOffset     =  -boxThickness/2.0f;
	protected static final int   cylinderResolution = 40;
	protected static PShape openCylinder            = new PShape();
	protected static PShape topCylinder             = new PShape();
	protected static PShape bottomCylinder          = new PShape();
	protected static PShape tree                    = new PShape();

	//Maximum value in degree for the angles along X and Z
	protected static final float maxTilt      = 60.0f;

	//Mouse wheel rotation speed
	protected static final float maxSpeed     = 1.5f;
	protected static final float minSpeed     = 0.2f;
	protected static final float speedScaling = 0.1f;

	//Constant for Y angle with keyboard
	protected static final float speedFactor  = 2;

	//Variables
	protected static float speed      = 1.0f;
	protected static float angleY     = 0.0f;
	protected static float tiltAngleX = 0.0f;
	protected static float tiltAngleZ = 0.0f;
	protected static float prevX      = 0.0f;
	protected static float prevZ      = 0.0f;
	protected static float begX       = 0.0f;
	protected static float begZ       = 0.0f;
	protected static boolean saveBeginDrag = true;

	protected static boolean addingCylinderMode = false;
	protected static boolean ignoreYRotation    = true;

	protected static ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();

	protected static final float ratio             = 1.0f/5.0f;
	protected static final int   bottomPanelColor  = 0xff4EA042;
	protected static final int   space               = 20;
	protected static final int   blankWidth          = 10;
	protected static final int   scoreBoxWidth       = 100;
	protected static BottomPanel bottomPanel;

	protected static float score = 0;
	protected static float lastScore = 0;
}

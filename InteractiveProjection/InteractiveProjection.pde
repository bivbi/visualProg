void setup() {
  size (1000,1000,P2D);
}
 
 float scaleRate = 1;
 float angleX = 0;
 float angleY = 0;
  
 ///// VIEW /////
 
void draw() {
  background(255,255,255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0);
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 300);
  
  float[][] translate = translationMatrix(width/2, height/2, 1);
  float[][] scale = scaleMatrix(scaleRate, scaleRate, scaleRate);
  float[][] rotateX = rotateXMatrix(angleX);
  float[][] rotateY = rotateYMatrix(angleY);
  
  input3DBox = transformBox(input3DBox, scale);
  input3DBox = transformBox(input3DBox, rotateX);
  input3DBox = transformBox(input3DBox, rotateY);
  input3DBox = transformBox(input3DBox, translate);

  projectBox(eye, input3DBox).render();
}

///// CONTROLLER /////

void mouseWheel(MouseEvent event) {
  float e = event.getCount();
  println(e);
  if (e >= 0) {
    scaleRate -= 0.02;
  } else {
    scaleRate += 0.02;
  } 
}

void keyPressed() {
  if (keyCode == UP) {
    angleX -= 0.02;
  } else if (keyCode == DOWN) {
    angleX += 0.02;
  } else if (keyCode == RIGHT) {
    angleY += 0.02;
  } else if (keyCode == LEFT) {
    angleY -= 0.02;
  }
}

///// MODEL /////
 
class My2DPoint {
  float x;
  float y;
 
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }  
}
 
My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {    
  float[][] T = {{1,0,0,-eye.x},
                 {0,1,0,-eye.y},
                 {0,0,1,-eye.z},
                 {0,0,0,1}};
                   
  float[][] P = {{1,0,0,0},
                 {0,1,0,0},
                 {0,0,1,0},
                 {0,0,1.0/(-eye.z),0}};
                 
  float[][] PT = multiplyMatrix(P, T);
  
  float[] this3DPoint = {p.x, p.y, p.z, 1};
  float[] projectedPoint = matrixProduct(PT, this3DPoint);
  
  return new My2DPoint(projectedPoint[0]*(eye.z/(eye.z -p.z)), projectedPoint[1]*(eye.z/(eye.z -p.z)));
  //return new My2DPoint((p.x*eye.z - eye.x*eye.z)/(eye.z - p.z), (p.y*eye.z - eye.y*eye.z)/(eye.z - p.z));
}
 
class My3DPoint {
  float x;
  float y;
  float z;
  My3DPoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
 
class My2DBox {
  My2DPoint[] s;
  My2DBox(My2DPoint[] s) {
    this.s = s;
  }
 
  void render() {
    strokeWeight(3);
    //back
    stroke(#10FF00);
    line(s[4].x, s[4].y, s[5].x, s[5].y);
    line(s[4].x, s[4].y, s[7].x, s[7].y);
    line(s[5].x, s[5].y, s[6].x, s[6].y);
    line(s[6].x, s[6].y, s[7].x, s[7].y);
    //sides
    stroke(#0024FF);
    line(s[0].x, s[0].y, s[4].x, s[4].y);
    line(s[1].x, s[1].y, s[5].x, s[5].y);
    line(s[2].x, s[2].y, s[6].x, s[6].y);
    line(s[3].x, s[3].y, s[7].x, s[7].y);
    //front face
    stroke(#FF0000);
    line(s[0].x, s[0].y, s[1].x, s[1].y);
    line(s[0].x, s[0].y, s[3].x, s[3].y);
    line(s[2].x, s[2].y, s[3].x, s[3].y);
    line(s[1].x, s[1].y, s[2].x, s[2].y);
  }
}
 
class My3DBox {
  My3DPoint[] p;
 
  My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ) {
    float x = origin.x;
    float y = origin.y;
    float z = origin.z;
   
    this.p = new My3DPoint[]{ new My3DPoint(x, y+dimY, z+dimZ),
                              new My3DPoint(x, y, z+dimZ),
                              new My3DPoint(x+dimX, y, z+dimZ),
                              new My3DPoint(x+dimX, y+dimY, z+dimZ),
                              new My3DPoint(x, y+dimY, z),
                              origin,
                              new My3DPoint(x+dimX, y, z),
                              new My3DPoint(x+dimX, y+dimY, z)
                            };
  }
 
  My3DBox(My3DPoint[] p) {
    this.p = p;
  }
}
 
 
 
My2DBox projectBox (My3DPoint eye, My3DBox box) {
    My2DPoint[] projected2DPoints = new My2DPoint[box.p.length];
    for(int i = 0; i < box.p.length; ++i) {
      projected2DPoints[i] = projectPoint(eye, box.p[i]);
    }
    return new My2DBox(projected2DPoints);
}
 
 
My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  float[][] nonHomogen3DPoints = new float[box.p.length][transformMatrix.length];
  for (int i = 0; i < box.p.length; ++i) {
    float[] b = {box.p[i].x, box.p[i].y, box.p[i].z, 1};
    nonHomogen3DPoints[i] = matrixProduct(transformMatrix, b);
  }
  
  My3DPoint[] homogen3DPoints = new My3DPoint[box.p.length];
  for (int i = 0; i < box.p.length; ++i) {
    homogen3DPoints[i] = euclidian3DPoint(nonHomogen3DPoints[i]);
  }
  
  return new My3DBox(homogen3DPoints);
}


 
///// UTILITY FUNCTIONS /////

float[][] multiplyMatrix(float[][] m1, float[][] m2) {
  if(m1[0].length != m2.length) {
    throw new IllegalArgumentException("Incorrect Dimmensions !");  
  }
  
  float sum = 0;
  float[][] m3 = new float [m1.length][m2[0].length];    

  for(int i=0; i<m1.length; i++) {
    for(int j=0; j<m2[0].length; j++) {
      for(int k=0; k<m2.length; k++) {
        sum += ( m1[i][k] * m2[k][j] );
      }
      m3[i][j] = sum;
      sum = 0;
    }
  }
  
  return m3;
}
 
float[] matrixProduct(float[][] a, float[] b) {
  float[] r = new float[b.length];
  for (int i = 0; i < b.length; ++i) {
    for (int j = 0; j < b.length; ++j) {
      r[i] += a[i][j]*b[j];
    }
  }
  return r;
}

float[] homogeneous3DPoint(My3DPoint p) {
  return new float[] {p.x, p.y, p.z, 1};
} 
 
My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}

///// MATRIX TRANSFORMATIONS /////

float[][] rotateXMatrix(float angle) {
  return( new float[][] {{1, 0, 0, 0},
                         {0, cos(angle), sin(angle), 0},
                         {0, -sin(angle), cos(angle), 0},
                         {0, 0, 0, 1}} );
}
 
float[][] rotateYMatrix(float angle) {
  return( new float[][] {{cos(angle), 0, sin(angle), 0},
                         {0, 1, 0, 0},
                         {-sin(angle), 0, cos(angle), 0},
                         {0, 0, 0, 1}} );
}
 
float[][] rotateZMatrix(float angle) {
  return( new float[][] {{cos(angle), -sin(angle), 0, 0},
                         {sin(angle), cos(angle), 0, 0},
                         {0, 0, 1, 0},
                         {0, 0, 0, 1}} );
}
 
float[][] scaleMatrix(float x, float y, float z) {
  return (new float[][] {{x, 0, 0, 0},
                         {0, y, 0, 0},
                         {0, 0, z, 0},
                         {0, 0, 0, 1}});
}
 
float[][] translationMatrix(float x, float y, float z) {
  return (new float[][] {{1, 0, 0, x},
                         {0, 1, 0, y},
                         {0, 0, 1, z},
                         {0, 0, 0, 1}});
}

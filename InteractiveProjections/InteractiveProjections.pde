void setup() {
  size (1000,1000,P2D);
}
  float scaleRate = 1;
  float angleX = 0;
  float angleY = 0;

void draw() {
  background(255,255,255);
  My3DPoint eye = new My3DPoint(0, 0, -5000);
  My3DPoint origin = new My3DPoint(0, 0, 0);
  My3DBox input3DBox = new My3DBox(origin, 100, 150, 300);
    
  /*//rotated around x
  float[][] transform1 = rotateXMatrix(PI/8);
  input3DBox = transformBox(input3DBox, transform1);
  projectBox(eye, input3DBox).render();
  
  //rotated and translated
  float[][] transform2 = translationMatrix(200,200,0);
  input3DBox = transformBox(input3DBox, transform2);
  projectBox(eye, input3DBox).render();
  
  //rotated, translated and scaled
  float[][] transform3 = scaleMatrix(2,2,2);
  input3DBox = transformBox(input3DBox, transform3);
  projectBox(eye, input3DBox).render();*/
  float[][] translate = translationMatrix(500,500,1);
  float[][] rotateX = rotateXMatrix(angleX);
  float[][] rotateY = rotateYMatrix(angleY);
  float[][] scale = scaleMatrix(scaleRate, scaleRate, scaleRate);
  input3DBox = transformBox(input3DBox, scale);
  input3DBox = transformBox(input3DBox, rotateX);
  input3DBox = transformBox(input3DBox, rotateY);  
  input3DBox = transformBox(input3DBox, translate);
  projectBox(eye, input3DBox).render();
}

void mouseDragged() {
  if(pmouseY < mouseY)
    scaleRate = scaleRate + 0.05;
  else if(pmouseY > mouseY)
    scaleRate = scaleRate - 0.05;
}

void keyPressed() {
  if(keyCode == UP) {
    angleX += 0.05;
  } else if (keyCode == DOWN) {
    angleX -= 0.05;
  } 
  if (keyCode == RIGHT) {
    angleY += 0.05;
  } else if(keyCode == LEFT) {
    angleY -= 0.05;
  } 
}


class My2DPoint {
  float x;
  float y;
  
  My2DPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }  
}

My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
    return new My2DPoint((p.x*eye.z - eye.x*eye.z)/(eye.z - p.z), (p.y*eye.z - eye.y*eye.z)/(eye.z - p.z));
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
    My2DPoint[] s = new My2DPoint[box.p.length];
    for(int i = 0; i < box.p.length; ++i) {
      s[i] = projectPoint(eye, box.p[i]);
    }
    return new My2DBox(s);
  }

float[] homogeneous3DPoint (My3DPoint p) {
  float[] result = {p.x, p.y, p.z, 1};
  return result;
}

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
  return (new float[][] {{x,0,0,0}, {0,y,0,0},
                          {0,0,z,0}, {0,0,0,1}});
}

float[][] translationMatrix(float x, float y, float z) {
  return (new float[][] {{1,0,0,x}, {0,1,0,y},
                          {0,0,1,z},{0,0,0,1}});
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


My3DBox transformBox(My3DBox box, float[][] transformMatrix) {
  float[][] r = new float[box.p.length][transformMatrix.length];
  for (int i = 0; i < box.p.length; ++i) {
    float[] b = {box.p[i].x, box.p[i].y, box.p[i].z, 1};
    r[i] = matrixProduct(transformMatrix, b);
  }
  My3DPoint[] p = new My3DPoint[r.length];
  for (int i = 0; i < r.length; ++i) {
    p[i] = euclidian3DPoint(r[i]);
  }
  return new My3DBox(p);
}

My3DPoint euclidian3DPoint (float[] a) {
  My3DPoint result = new My3DPoint(a[0]/a[3], a[1]/a[3], a[2]/a[3]);
  return result;
}


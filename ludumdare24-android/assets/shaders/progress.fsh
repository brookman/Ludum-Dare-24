#ifdef GL_ES
   #define LOWP lowp
   precision mediump float;
#else
   #define LOWP
#endif


uniform float uProgress;
uniform vec4 uLeftColor;
uniform vec4 uRightColor;

varying vec2 vTextureCoord;

void main(void) {

	if(1.0 - vTextureCoord.x < uProgress) {
		gl_FragColor = uLeftColor;
	} else {
		gl_FragColor = uRightColor;
	}
}
attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

varying vec2 vTextureCoord;

void main(void) {
	vec4 vPosition = uMVMatrix * vec4(a_position, 1.0);
	gl_Position = uPMatrix * vPosition;
	vTextureCoord = a_texCoord0;
}
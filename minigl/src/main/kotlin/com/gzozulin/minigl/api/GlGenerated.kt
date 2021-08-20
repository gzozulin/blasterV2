package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.PhongMaterial

private const val DEF_RAY = "struct ray {  vec3 origin ; vec3 direction ;  };\n"
private const val DEF_AABB = "struct aabb {  vec3 pointMin ; vec3 pointMax ;  };\n"
private const val DEF_CAMERA = "struct Camera {  vec3 origin ; vec3 lowerLeft ; vec3 horizontal ; vec3 vertical ; vec3 w , u , v ; float lensRadius ;  };\n"
private const val DEF_LIGHT = "struct Light {  vec3 vector ; vec3 color ; float attenConstant ; float attenLinear ; float attenQuadratic ;  };\n"
private const val DEF_PHONGMATERIAL = "struct PhongMaterial {  vec3 ambient ; vec3 diffuse ; vec3 specular ; float shine ; float transparency ;  };\n"
private const val DEF_BVHNODE = "struct BvhNode {  aabb aabb ; int leftType ; int leftIndex ; int rightType ; int rightIndex ;  };\n"
private const val DEF_SPHERE = "struct Sphere {  vec3 center ; float radius ; int materialType ; int materialIndex ;  };\n"
private const val DEF_LAMBERTIANMATERIAL = "struct LambertianMaterial {  vec3 albedo ;  };\n"
private const val DEF_METALLICMATERIAL = "struct MetallicMaterial {  vec3 albedo ;  };\n"
private const val DEF_DIELECTRICMATERIAL = "struct DielectricMaterial {  float reflectiveIndex ;  };\n"
private const val DEF_HITRECORD = "struct HitRecord {  float t ; vec3 point ; vec3 normal ; int materialType ; int materialIndex ;  };\n"
private const val DEF_SCATTERRESULT = "struct ScatterResult {  vec3 attenuation ; ray scattered ;  };\n"
private const val DEF_REFRACTRESULT = "struct RefractResult {  bool isRefracted ; vec3 refracted ;  };\n"
private const val DEF_ADDF = "float addf ( float left , float right ) { return left + right ; }\n"
private const val DEF_SUBF = "float subf ( float left , float right ) { return left - right ; }\n"
private const val DEF_MULF = "float mulf ( float left , float right ) { return left * right ; }\n"
private const val DEF_DIVF = "float divf ( float left , float right ) { return left / right ; }\n"
private const val DEF_EQV2 = "bool eqv2 ( vec2 left , vec2 right ) { return left . x == right . x && left . y == right . y ; }\n"
private const val DEF_EQIV2 = "bool eqiv2 ( ivec2 left , ivec2 right ) { return left . x == right . x && left . y == right . y ; }\n"
private const val DEF_EQV3 = "bool eqv3 ( vec3 left , vec3 right ) { return left . x == right . x && left . y == right . y && left . z == right . z ; }\n"
private const val DEF_EQV4 = "bool eqv4 ( vec4 left , vec4 right ) { return left . x == right . x && left . y == right . y && left . z == right . z && left . w == right . w ; }\n"
private const val DEF_SCHLICKF = "float schlickf ( float cosine , float ri ) { float r0 = ( 1 - ri ) / ( 1 + ri ) ; r0 = r0 * r0 ; return r0 + ( 1 - r0 ) * powf ( ( 1 - cosine ) , 5 ) ; }\n"
private const val DEF_REMAPF = "float remapf ( float a , float b , float c , float d , float t ) { return ( ( t - a ) / ( b - a ) ) * ( d - c ) + c ; }\n"
private const val DEF_FTOV2 = "vec2 ftov2 ( float v ) { return v2 ( v , v ) ; }\n"
private const val DEF_V2ZERO = "vec2 v2zero ( ) { return ftov2 ( 0.0f ) ; }\n"
private const val DEF_ADDV2 = "vec2 addv2 ( vec2 l , vec2 r ) { return v2 ( l . x + r . x , l . y + r . y ) ; }\n"
private const val DEF_DIVV2 = "vec2 divv2 ( vec2 left , vec2 right ) { return v2 ( left . x / right . x , left . y / right . y ) ; }\n"
private const val DEF_DIVV2F = "vec2 divv2f ( vec2 v , float f ) { return v2 ( v . x / f , v . y / f ) ; }\n"
private const val DEF_GETXV2 = "float getxv2 ( vec2 v ) { return v . x ; }\n"
private const val DEF_GETYV2 = "float getyv2 ( vec2 v ) { return v . y ; }\n"
private const val DEF_LENV2 = "float lenv2 ( vec2 v ) { return sqrtf ( v . x * v . x + v . y * v . y ) ; }\n"
private const val DEF_INDEXV3 = "float indexv3 ( vec3 v , int index ) { switch ( index ) { case 0 : return v . x ; case 1 : return v . y ; case 2 : return v . z ; default : error ( ) ; return v . x ; } }\n"
private const val DEF_V2TOV3 = "vec3 v2tov3 ( vec2 v , float f ) { return v3 ( v . x , v . y , f ) ; }\n"
private const val DEF_FTOV3 = "vec3 ftov3 ( float v ) { return v3 ( v , v , v ) ; }\n"
private const val DEF_V3ZERO = "vec3 v3zero ( ) { return ftov3 ( 0.0f ) ; }\n"
private const val DEF_V3ONE = "vec3 v3one ( ) { return ftov3 ( 1.0f ) ; }\n"
private const val DEF_V3FRONT = "vec3 v3front ( ) { return v3 ( 0 , 0 , 1 ) ; }\n"
private const val DEF_V3BACK = "vec3 v3back ( ) { return v3 ( 0 , 0 , - 1 ) ; }\n"
private const val DEF_V3LEFT = "vec3 v3left ( ) { return v3 ( - 1 , 0 , 0 ) ; }\n"
private const val DEF_V3RIGHT = "vec3 v3right ( ) { return v3 ( 1 , 0 , 0 ) ; }\n"
private const val DEF_V3UP = "vec3 v3up ( ) { return v3 ( 0 , 1 , 0 ) ; }\n"
private const val DEF_V3DOWN = "vec3 v3down ( ) { return v3 ( 0 , - 1 , 0 ) ; }\n"
private const val DEF_V3WHITE = "vec3 v3white ( ) { return v3 ( 1.0f , 1.0f , 1.0f ) ; }\n"
private const val DEF_V3BLACK = "vec3 v3black ( ) { return v3 ( 0.0f , 0.0f , 0.0f ) ; }\n"
private const val DEF_V3LTGREY = "vec3 v3ltGrey ( ) { return ftov3 ( 0.3f ) ; }\n"
private const val DEF_V3GREY = "vec3 v3grey ( ) { return ftov3 ( 0.5f ) ; }\n"
private const val DEF_V3DKGREY = "vec3 v3dkGrey ( ) { return ftov3 ( 0.7f ) ; }\n"
private const val DEF_V3RED = "vec3 v3red ( ) { return v3 ( 1.0f , 0.0f , 0.0f ) ; }\n"
private const val DEF_V3GREEN = "vec3 v3green ( ) { return v3 ( 0.0f , 1.0f , 0.0f ) ; }\n"
private const val DEF_V3BLUE = "vec3 v3blue ( ) { return v3 ( 0.0f , 0.0f , 1.0f ) ; }\n"
private const val DEF_V3YELLOW = "vec3 v3yellow ( ) { return v3 ( 1.0f , 1.0f , 0.0f ) ; }\n"
private const val DEF_V3MAGENTA = "vec3 v3magenta ( ) { return v3 ( 1.0f , 0.0f , 1.0f ) ; }\n"
private const val DEF_V3CYAN = "vec3 v3cyan ( ) { return v3 ( 0.0f , 1.0f , 1.0f ) ; }\n"
private const val DEF_V3ORANGE = "vec3 v3orange ( ) { return v3 ( 1.0f , 0.5f , 0.0f ) ; }\n"
private const val DEF_V3ROSE = "vec3 v3rose ( ) { return v3 ( 1.0f , 0.0f , 0.5f ) ; }\n"
private const val DEF_V3VIOLET = "vec3 v3violet ( ) { return v3 ( 0.5f , 0.0f , 1.0f ) ; }\n"
private const val DEF_V3AZURE = "vec3 v3azure ( ) { return v3 ( 0.0f , 0.5f , 1.0f ) ; }\n"
private const val DEF_V3AQUAMARINE = "vec3 v3aquamarine ( ) { return v3 ( 0.0f , 1.0f , 0.5f ) ; }\n"
private const val DEF_V3CHARTREUSE = "vec3 v3chartreuse ( ) { return v3 ( 0.5f , 1.0f , 0.0f ) ; }\n"
private const val DEF_XYV3 = "vec2 xyv3 ( vec3 vec ) { return v2 ( vec . x , vec . y ) ; }\n"
private const val DEF_XZV3 = "vec2 xzv3 ( vec3 vec ) { return v2 ( vec . x , vec . z ) ; }\n"
private const val DEF_YZV3 = "vec2 yzv3 ( vec3 vec ) { return v2 ( vec . y , vec . z ) ; }\n"
private const val DEF_ABSV3 = "vec3 absv3 ( vec3 v ) { return v3 ( absf ( v . x ) , absf ( v . y ) , absf ( v . z ) ) ; }\n"
private const val DEF_NEGV3 = "vec3 negv3 ( vec3 v ) { return v3 ( - v . x , - v . y , - v . z ) ; }\n"
private const val DEF_SUBV3F = "vec3 subv3f ( vec3 left , float right ) { return subv3 ( left , ftov3 ( right ) ) ; }\n"
private const val DEF_POWV3 = "vec3 powv3 ( vec3 left , vec3 right ) { return v3 ( powf ( left . x , right . x ) , powf ( left . y , right . y ) , powf ( left . z , right . z ) ) ; }\n"
private const val DEF_MIXV3 = "vec3 mixv3 ( vec3 left , vec3 right , float proportion ) { return addv3 ( mulv3 ( left , ftov3 ( 1.0f - proportion ) ) , mulv3 ( right , ftov3 ( proportion ) ) ) ; }\n"
private const val DEF_MAXV3 = "vec3 maxv3 ( vec3 left , vec3 right ) { return v3 ( maxf ( left . x , right . x ) , maxf ( left . y , right . y ) , maxf ( left . z , right . z ) ) ; }\n"
private const val DEF_MINV3 = "vec3 minv3 ( vec3 left , vec3 right ) { return v3 ( minf ( left . x , right . x ) , minf ( left . y , right . y ) , minf ( left . z , right . z ) ) ; }\n"
private const val DEF_LENV3 = "float lenv3 ( vec3 v ) { return sqrtf ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n"
private const val DEF_SQRTV3 = "vec3 sqrtv3 ( vec3 v ) { return v3 ( sqrtf ( v . x ) , sqrtf ( v . y ) , sqrtf ( v . z ) ) ; }\n"
private const val DEF_LENSQV3 = "float lensqv3 ( vec3 v ) { return ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n"
private const val DEF_NORMV3 = "vec3 normv3 ( vec3 v ) { return divv3f ( v , lenv3 ( v ) ) ; }\n"
private const val DEF_LERPV3 = "vec3 lerpv3 ( vec3 from , vec3 to , float t ) { return addv3 ( mulv3f ( from , 1.0f - t ) , mulv3f ( to , t ) ) ; }\n"
private const val DEF_REFLECTV3 = "vec3 reflectv3 ( vec3 v , vec3 n ) { return subv3 ( v , mulv3f ( n , 2.0f * dotv3 ( v , n ) ) ) ; }\n"
private const val DEF_REFRACTV3 = "RefractResult refractv3 ( vec3 v , vec3 n , float niOverNt ) { vec3 unitV = normv3 ( v ) ; float dt = dotv3 ( unitV , n ) ; float D = 1.0f - niOverNt * niOverNt * ( 1.0f - dt * dt ) ; if ( D > 0 ) { vec3 left = mulv3f ( subv3 ( unitV , mulv3f ( n , dt ) ) , niOverNt ) ; vec3 right = mulv3f ( n , sqrtf ( D ) ) ; RefractResult result = { true , subv3 ( left , right ) } ; return result ; } else { return NO_REFRACT ; } }\n"
private const val DEF_V3TOV4 = "vec4 v3tov4 ( vec3 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n"
private const val DEF_FTOV4 = "vec4 ftov4 ( float v ) { return v4 ( v , v , v , v ) ; }\n"
private const val DEF_V4TOV3 = "vec3 v4tov3 ( vec4 v ) { return v3 ( v . x , v . y , v . z ) ; }\n"
private const val DEF_V4ZERO = "vec4 v4zero ( ) { return ftov4 ( 0.0f ) ; }\n"
private const val DEF_V4ONE = "vec4 v4one ( ) { return ftov4 ( 1.0f ) ; }\n"
private const val DEF_ADDV4 = "vec4 addv4 ( vec4 left , vec4 right ) { return v4 ( left . x + right . x , left . y + right . y , left . z + right . z , left . w + right . w ) ; }\n"
private const val DEF_SUBV4 = "vec4 subv4 ( vec4 left , vec4 right ) { return v4 ( left . x - right . x , left . y - right . y , left . z - right . z , left . w - right . w ) ; }\n"
private const val DEF_MULV4 = "vec4 mulv4 ( vec4 left , vec4 right ) { return v4 ( left . x * right . x , left . y * right . y , left . z * right . z , left . w * right . w ) ; }\n"
private const val DEF_MULV4F = "vec4 mulv4f ( vec4 left , float right ) { return v4 ( left . x * right , left . y * right , left . z * right , left . w * right ) ; }\n"
private const val DEF_DIVV4 = "vec4 divv4 ( vec4 left , vec4 right ) { return v4 ( left . x / right . x , left . y / right . y , left . z / right . z , left . w / right . w ) ; }\n"
private const val DEF_DIVV4F = "vec4 divv4f ( vec4 left , float right ) { return v4 ( left . x / right , left . y / right , left . z / right , left . z / right ) ; }\n"
private const val DEF_GETXV4 = "float getxv4 ( vec4 v ) { return v . x ; }\n"
private const val DEF_GETYV4 = "float getyv4 ( vec4 v ) { return v . y ; }\n"
private const val DEF_GETZV4 = "float getzv4 ( vec4 v ) { return v . z ; }\n"
private const val DEF_GETWV4 = "float getwv4 ( vec4 v ) { return v . w ; }\n"
private const val DEF_GETRV4 = "float getrv4 ( vec4 v ) { return v . x ; }\n"
private const val DEF_GETGV4 = "float getgv4 ( vec4 v ) { return v . y ; }\n"
private const val DEF_GETBV4 = "float getbv4 ( vec4 v ) { return v . z ; }\n"
private const val DEF_GETAV4 = "float getav4 ( vec4 v ) { return v . w ; }\n"
private const val DEF_SETXV4 = "vec4 setxv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n"
private const val DEF_SETYV4 = "vec4 setyv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n"
private const val DEF_SETZV4 = "vec4 setzv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n"
private const val DEF_SETWV4 = "vec4 setwv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n"
private const val DEF_SETRV4 = "vec4 setrv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n"
private const val DEF_SETGV4 = "vec4 setgv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n"
private const val DEF_SETBV4 = "vec4 setbv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n"
private const val DEF_SETAV4 = "vec4 setav4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n"
private const val DEF_IV2ZERO = "ivec2 iv2zero ( ) { return iv2 ( 0 , 0 ) ; }\n"
private const val DEF_IV2TOV2 = "vec2 iv2tov2 ( ivec2 v ) { return v2 ( itof ( v . x ) , itof ( v . y ) ) ; }\n"
private const val DEF_IV2TOV4 = "vec4 iv2tov4 ( ivec2 vec , float z , float w ) { return v4 ( itof ( vec . x ) , itof ( vec . y ) , z , w ) ; }\n"
private const val DEF_GETXIV2 = "float getxiv2 ( vec2 v ) { return v . x ; }\n"
private const val DEF_GETYIV2 = "float getyiv2 ( vec2 v ) { return v . y ; }\n"
private const val DEF_GETUIV2 = "float getuiv2 ( vec2 v ) { return v . x ; }\n"
private const val DEF_GETVIV2 = "float getviv2 ( vec2 v ) { return v . y ; }\n"
private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; return v2 ( tileStartX + texCoord . x * tileSideX , tileStartY + texCoord . y * tileSideY ) ; }\n"
private const val DEF_RAYBACK = "ray rayBack ( ) { ray result = { v3zero ( ) , v3back ( ) } ; return result ; }\n"
private const val DEF_RAYPOINT = "vec3 rayPoint ( ray ray , float t ) { return addv3 ( ray . origin , mulv3f ( ray . direction , t ) ) ; }\n"
private const val DEF_SDXZPLANE = "float sdXZPlane ( vec3 p ) { return p . y ; }\n"
private const val DEF_SDSPHERE = "float sdSphere ( vec3 p , float r ) { return lenv3 ( p ) - r ; }\n"
private const val DEF_SDBOX = "float sdBox ( vec3 p , vec3 b ) { vec3 q = subv3 ( absv3 ( p ) , b ) ; return lenv3 ( maxv3 ( q , v3zero ( ) ) ) + minf ( maxf ( q . x , maxf ( q . y , q . z ) ) , 0.0f ) ; }\n"
private const val DEF_SDCAPPEDCYLINDER = "float sdCappedCylinder ( vec3 p , vec3 a , vec3 b , float r ) { vec3 ba = subv3 ( b , a ) ; vec3 pa = subv3 ( p , a ) ; float baba = dotv3 ( ba , ba ) ; float paba = dotv3 ( pa , ba ) ; float x = lenv3 ( subv3 ( mulv3f ( pa , baba ) , mulv3f ( ba , paba ) ) ) - r * baba ; float y = absf ( paba - baba * 0.5f ) - baba * 0.5f ; float x2 = x * x ; float y2 = y * y * baba ; float d = ( maxf ( x , y ) < 0.0f ) ? - minf ( x2 , y2 ) : ( ( ( x > 0.0f ) ? x2 : 0.0f ) + ( ( y > 0.0f ) ? y2 : 0.0f ) ) ; return signf ( d ) * sqrtf ( absf ( d ) ) / baba ; }\n"
private const val DEF_SDCONE = "float sdCone ( vec3 p , vec2 c , float h ) { vec2 q = mulv2f ( v2 ( c . x / c . y , - 1.0f ) , h ) ; vec2 w = v2 ( lenv2 ( xzv3 ( p ) ) , p . y ) ; vec2 a = subv2 ( w , mulv2f ( q , clampf ( dotv2 ( w , q ) / dotv2 ( q , q ) , 0.0f , 1.0f ) ) ) ; vec2 b = subv2 ( w , mulv2 ( q , v2 ( clampf ( w . x / q . x , 0.0f , 1.0f ) , 1.0f ) ) ) ; float k = signf ( q . y ) ; float d = minf ( dotv2 ( a , a ) , dotv2 ( b , b ) ) ; float s = maxf ( k * ( w . x * q . y - w . y * q . x ) , k * ( w . y - q . y ) ) ; return sqrtf ( d ) * signf ( s ) ; }\n"
private const val DEF_SDTRIPRISM = "float sdTriPrism ( vec3 p , vec2 h ) { vec3 q = absv3 ( p ) ; return maxf ( q . z - h . y , maxf ( q . x * 0.866025f + p . y * 0.5f , - p . y ) - h . x * 0.5f ) ; }\n"
private const val DEF_OPUNION = "float opUnion ( float d1 , float d2 ) { return minf ( d1 , d2 ) ; }\n"
private const val DEF_OPSUBTRACTION = "float opSubtraction ( float d1 , float d2 ) { return maxf ( - d1 , d2 ) ; }\n"
private const val DEF_OPINTERSECTION = "float opIntersection ( float d1 , float d2 ) { return maxf ( d1 , d2 ) ; }\n"
private const val DEF_RANDOMINUNITSPHERE = "vec3 randomInUnitSphere ( ) { vec3 result ; for ( int i = 0 ; i < 10 ; i ++ ) { result = v3 ( seededRndf ( ) * 2.0f - 1.0f , seededRndf ( ) * 2.0f - 1.0f , seededRndf ( ) * 2.0f - 1.0f ) ; if ( lensqv3 ( result ) >= 1.0f ) { return result ; } } return normv3 ( result ) ; }\n"
private const val DEF_RANDOMINUNITDISK = "vec3 randomInUnitDisk ( ) { vec3 result ; for ( int i = 0 ; i < 10 ; i ++ ) { result = subv3 ( mulv3f ( v3 ( seededRndf ( ) , seededRndf ( ) , 0.0f ) , 2.0f ) , v3 ( 1.0f , 1.0f , 0.0f ) ) ; if ( dotv3 ( result , result ) >= 1.0f ) { return result ; } } return normv3 ( result ) ; }\n"
private const val DEF_CENTERUV = "vec2 centerUV ( vec2 uv , float aspect ) { vec2 center = subv2f ( uv , 0.5f ) ; return v2 ( center . x * aspect , center . y ) ; }\n"
private const val DEF_CAMERALOOKAT = "Camera cameraLookAt ( vec3 eye , vec3 center , vec3 up , float fovy , float aspect , float aperture , float focusDist ) { float lensRadius = aperture / 2.0f ; float halfHeight = tanf ( fovy / 2.0f ) ; float halfWidth = aspect * halfHeight ; vec3 w = normv3 ( subv3 ( eye , center ) ) ; vec3 u = normv3 ( crossv3 ( up , w ) ) ; vec3 v = crossv3 ( w , u ) ; vec3 hwu = mulv3f ( u , halfWidth * focusDist ) ; vec3 hhv = mulv3f ( v , halfHeight * focusDist ) ; vec3 wf = mulv3f ( w , focusDist ) ; vec3 lowerLeft = subv3 ( subv3 ( subv3 ( eye , hwu ) , hhv ) , wf ) ; vec3 horizontal = mulv3f ( u , halfWidth * focusDist * 2.0f ) ; vec3 vertical = mulv3f ( v , halfHeight * focusDist * 2.0f ) ; Camera result = { eye , lowerLeft , horizontal , vertical , w , u , v , lensRadius } ; return result ; }\n"
private const val DEF_RAYFROMCAMERA = "ray rayFromCamera ( Camera camera , vec2 uv ) { vec3 horShift = mulv3f ( camera . horizontal , uv . x ) ; vec3 verShift = mulv3f ( camera . vertical , uv . y ) ; vec3 origin ; vec3 direction ; if ( camera . lensRadius > 0.0f ) { vec3 rd = mulv3f ( randomInUnitDisk ( ) , camera . lensRadius ) ; vec3 offset = addv3 ( mulv3f ( camera . u , rd . x ) , mulv3f ( camera . v , rd . y ) ) ; origin = addv3 ( camera . origin , offset ) ; direction = normv3 ( subv3 ( subv3 ( addv3 ( camera . lowerLeft , addv3 ( horShift , verShift ) ) , camera . origin ) , offset ) ) ; } else { origin = camera . origin ; direction = normv3 ( subv3 ( addv3 ( camera . lowerLeft , addv3 ( horShift , verShift ) ) , camera . origin ) ) ; } ray result = { origin , direction } ; return result ; }\n"
private const val DEF_PI = "float PI = 3.1415f ;\n"
private const val DEF_BOUNCE_ERR = "float BOUNCE_ERR = 0.001f ;\n"
private const val DEF_NO_HIT = "HitRecord NO_HIT = { - 1 , { 0 , 0 , 0 } , { 1 , 0 , 0 } , 0 , 0 } ;\n"
private const val DEF_NO_SCATTER = "ScatterResult NO_SCATTER = { { - 1 , - 1 , - 1 } , { { 0 , 0 , 0 } , { 0 , 0 , 0 } } } ;\n"
private const val DEF_NO_REFRACT = "RefractResult NO_REFRACT = { false , { 0 , 0 , 0 } } ;\n"
private const val DEF_BACKGROUND = "vec3 background ( ray ray ) { float t = ( ray . direction . y + 1.0f ) * 0.5f ; vec3 gradient = lerpv3 ( v3one ( ) , v3 ( 0.5f , 0.7f , 1.0f ) , t ) ; return gradient ; }\n"
private const val DEF_RAYHITAABB = "bool rayHitAabb ( ray ray , aabb aabb , float tMin , float tMax ) { for ( int i = 0 ; i < 3 ; i ++ ) { float invD = 1.0f / indexv3 ( ray . direction , i ) ; float t0 = ( indexv3 ( aabb . pointMin , i ) - indexv3 ( ray . origin , i ) ) * invD ; float t1 = ( indexv3 ( aabb . pointMax , i ) - indexv3 ( ray . origin , i ) ) * invD ; if ( invD < 0.0f ) { float temp = t0 ; t0 = t1 ; t1 = temp ; } float tmin = t0 > tMin ? t0 : tMin ; float tmax = t1 < tMax ? t1 : tMax ; if ( tmax <= tmin ) { return false ; } } return true ; }\n"
private const val DEF_RAYHITSPHERERECORD = "HitRecord rayHitSphereRecord ( ray ray , float t , Sphere sphere ) { vec3 point = rayPoint ( ray , t ) ; vec3 N = normv3 ( divv3f ( subv3 ( point , sphere . center ) , sphere . radius ) ) ; HitRecord result = { t , point , N , sphere . materialType , sphere . materialIndex } ; return result ; }\n"
private const val DEF_RAYHITSPHERE = "HitRecord rayHitSphere ( ray ray , float tMin , float tMax , Sphere sphere ) { vec3 oc = subv3 ( ray . origin , sphere . center ) ; float a = dotv3 ( ray . direction , ray . direction ) ; float b = 2 * dotv3 ( oc , ray . direction ) ; float c = dotv3 ( oc , oc ) - sphere . radius * sphere . radius ; float D = b * b - 4 * a * c ; if ( D > 0 ) { float t = ( - b - sqrtf ( D ) ) / 2 * a ; if ( t < tMax && t > tMin ) { return rayHitSphereRecord ( ray , t , sphere ) ; } t = ( - b + sqrtf ( D ) ) / 2 * a ; if ( t < tMax && t > tMin ) { return rayHitSphereRecord ( ray , t , sphere ) ; } } return NO_HIT ; }\n"
private const val DEF_RAYHITOBJECT = "HitRecord rayHitObject ( ray ray , float tMin , float tMax , int type , int index ) { if ( type != HITABLE_SPHERE ) { error ( ) ; return NO_HIT ; } return rayHitSphere ( ray , tMin , tMax , uSpheres [ index ] ) ; }\n"
private const val DEF_RAYHITBVH = "HitRecord rayHitBvh ( ray ray , float tMin , float tMax , int index ) { bvhTop = 0 ; float closest = tMax ; HitRecord result = NO_HIT ; int curr = index ; while ( curr >= 0 ) { while ( curr >= 0 && rayHitAabb ( ray , uBvhNodes [ curr ] . aabb , tMin , closest ) ) { if ( uBvhNodes [ curr ] . leftType == HITABLE_BVH ) { bvhStack [ bvhTop ] = curr ; bvhTop ++ ; curr = uBvhNodes [ curr ] . leftIndex ; } else { HitRecord hit = rayHitObject ( ray , tMin , closest , uBvhNodes [ curr ] . leftType , uBvhNodes [ curr ] . leftIndex ) ; if ( hit . t > 0 && hit . t < closest ) { result = hit ; closest = hit . t ; } break ; } } bvhTop -- ; if ( bvhTop < 0 ) { break ; } curr = bvhStack [ bvhTop ] ; curr = uBvhNodes [ curr ] . rightIndex ; } return result ; }\n"
private const val DEF_RAYHITWORLD = "HitRecord rayHitWorld ( ray ray , float tMin , float tMax ) { return rayHitBvh ( ray , tMin , tMax , 0 ) ; }\n"
private const val DEF_SCATTERLAMBERTIAN = "ScatterResult scatterLambertian ( HitRecord record , LambertianMaterial material ) { vec3 tangent = addv3 ( record . point , record . normal ) ; vec3 direction = addv3 ( tangent , randomInUnitSphere ( ) ) ; ScatterResult result = { material . albedo , { record . point , subv3 ( direction , record . point ) } } ; return result ; }\n"
private const val DEF_SCATTERMETALLIC = "ScatterResult scatterMetallic ( ray ray , HitRecord record , MetallicMaterial material ) { vec3 reflected = reflectv3 ( ray . direction , record . normal ) ; if ( dotv3 ( reflected , record . normal ) > 0 ) { ScatterResult result = { material . albedo , { record . point , reflected } } ; return result ; } else { return NO_SCATTER ; } }\n"
private const val DEF_SCATTERDIELECTRIC = "ScatterResult scatterDielectric ( ray ray , HitRecord record , DielectricMaterial material ) { float niOverNt ; float cosine ; vec3 outwardNormal ; float rdotn = dotv3 ( ray . direction , record . normal ) ; float dirlen = lenv3 ( ray . direction ) ; if ( rdotn > 0 ) { outwardNormal = negv3 ( record . normal ) ; niOverNt = material . reflectiveIndex ; cosine = material . reflectiveIndex * rdotn / dirlen ; } else { outwardNormal = record . normal ; niOverNt = 1.0f / material . reflectiveIndex ; cosine = - rdotn / dirlen ; } float reflectProbe ; RefractResult refractResult = refractv3 ( ray . direction , outwardNormal , niOverNt ) ; if ( refractResult . isRefracted ) { reflectProbe = schlickf ( cosine , material . reflectiveIndex ) ; } else { reflectProbe = 1.0f ; } vec3 scatteredDir ; if ( seededRndf ( ) < reflectProbe ) { scatteredDir = reflectv3 ( ray . direction , record . normal ) ; } else { scatteredDir = refractResult . refracted ; } ScatterResult scatterResult = { v3one ( ) , { record . point , scatteredDir } } ; return scatterResult ; }\n"
private const val DEF_SCATTERMATERIAL = "ScatterResult scatterMaterial ( ray ray , HitRecord record ) { switch ( record . materialType ) { case MATERIAL_LAMBERTIAN : return scatterLambertian ( record , uLambertianMaterials [ record . materialIndex ] ) ; case MATERIAL_METALIIC : return scatterMetallic ( ray , record , uMetallicMaterials [ record . materialIndex ] ) ; case MATERIAL_DIELECTRIC : return scatterDielectric ( ray , record , uDielectricMaterials [ record . materialIndex ] ) ; default : return NO_SCATTER ; } }\n"
private const val DEF_SAMPLECOLOR = "vec3 sampleColor ( int rayBounces , Camera camera , vec2 uv ) { ray ray = rayFromCamera ( camera , uv ) ; vec3 fraction = ftov3 ( 1.0f ) ; for ( int i = 0 ; i < rayBounces ; i ++ ) { HitRecord record = rayHitWorld ( ray , BOUNCE_ERR , FLT_MAX ) ; if ( record . t < 0 ) { break ; } else { ScatterResult scatterResult = scatterMaterial ( ray , record ) ; if ( scatterResult . attenuation . x < 0 ) { return v3zero ( ) ; } fraction = mulv3 ( fraction , scatterResult . attenuation ) ; ray = scatterResult . scattered ; } } return mulv3 ( background ( ray ) , fraction ) ; }\n"
private const val DEF_FRAGMENTCOLORRT = "vec4 fragmentColorRt ( int width , int height , float random , int sampleCnt , int rayBounces , vec3 eye , vec3 center , vec3 up , float fovy , float aspect , float aperture , float focusDist , vec2 texCoord ) { seedRandom ( v2tov3 ( texCoord , random ) ) ; float DU = 1.0f / itof ( width ) ; float DV = 1.0f / itof ( height ) ; Camera camera = cameraLookAt ( eye , center , up , fovy , aspect , aperture , focusDist ) ; vec3 result = v3zero ( ) ; for ( int i = 0 ; i < sampleCnt ; i ++ ) { float du = DU * seededRndf ( ) ; float dv = DV * seededRndf ( ) ; vec2 uv = addv2 ( texCoord , v2 ( du , dv ) ) ; result = addv3 ( result , sampleColor ( rayBounces , camera , uv ) ) ; } return v3tov4 ( result , 1.0f ) ; }\n"
private const val DEF_GAMMASQRT = "vec4 gammaSqrt ( vec4 result ) { return v4 ( sqrtf ( result . x ) , sqrtf ( result . y ) , sqrtf ( result . z ) , 1.0f ) ; }\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n"
private const val DEF_DIFFUSECONTRIB = "vec3 diffuseContrib ( vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { float diffuseTerm = dotv3 ( fragNormal , lightDir ) ; return diffuseTerm > 0.0f ? mulv3f ( material . diffuse , diffuseTerm ) : v3zero ( ) ; }\n"
private const val DEF_HALFVECTOR = "vec3 halfVector ( vec3 left , vec3 right ) { return normv3 ( addv3 ( left , right ) ) ; }\n"
private const val DEF_SPECULARCONTRIB = "vec3 specularContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { vec3 hv = halfVector ( viewDir , lightDir ) ; float specularTerm = dotv3 ( hv , fragNormal ) ; return specularTerm > 0.0f ? mulv3f ( material . specular , powf ( specularTerm , material . shine ) ) : v3zero ( ) ; }\n"
private const val DEF_LIGHTCONTRIB = "vec3 lightContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , float attenuation , Light light , PhongMaterial material ) { vec3 lighting = v3zero ( ) ; lighting = addv3 ( lighting , diffuseContrib ( lightDir , fragNormal , material ) ) ; lighting = addv3 ( lighting , specularContrib ( viewDir , lightDir , fragNormal , material ) ) ; return mulv3 ( mulv3f ( light . color , attenuation ) , lighting ) ; }\n"
private const val DEF_POINTLIGHTCONTRIB = "vec3 pointLightContrib ( vec3 viewDir , vec3 fragPosition , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 direction = subv3 ( light . vector , fragPosition ) ; vec3 lightDir = normv3 ( direction ) ; if ( dotv3 ( lightDir , fragNormal ) < 0.0f ) { return v3zero ( ) ; } float distance = lenv3 ( direction ) ; float lum = luminosity ( distance , light ) ; return lightContrib ( viewDir , lightDir , fragNormal , lum , light , material ) ; }\n"
private const val DEF_DIRLIGHTCONTRIB = "vec3 dirLightContrib ( vec3 viewDir , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 lightDir = negv3 ( normv3 ( light . vector ) ) ; return lightContrib ( viewDir , lightDir , fragNormal , 1.0f , light , material ) ; }\n"
private const val DEF_SHADINGFLAT = "vec4 shadingFlat ( vec4 color ) { return color ; }\n"
private const val DEF_SHADINGPHONG = "vec4 shadingPhong ( vec3 fragPosition , vec3 eye , vec3 fragNormal , vec3 fragAlbedo , PhongMaterial material ) { vec3 viewDir = normv3 ( subv3 ( eye , fragPosition ) ) ; vec3 color = material . ambient ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { color = addv3 ( color , pointLightContrib ( viewDir , fragPosition , fragNormal , uLights [ i ] , material ) ) ; } for ( int i = uLightsPointCnt ; i < uLightsPointCnt + uLightsDirCnt ; ++ i ) { color = addv3 ( color , dirLightContrib ( viewDir , fragNormal , uLights [ i ] , material ) ) ; } color = mulv3 ( color , fragAlbedo ) ; return v3tov4 ( color , material . transparency ) ; }\n"
private const val DEF_DISTRIBUTIONGGX = "float distributionGGX ( vec3 N , vec3 H , float a ) { float a2 = a * a ; float NdotH = maxf ( dotv3 ( N , H ) , 0.0f ) ; float NdotH2 = NdotH * NdotH ; float nom = a2 ; float denom = ( NdotH2 * ( a2 - 1.0f ) + 1.0f ) ; denom = PI * denom * denom ; return nom / denom ; }\n"
private const val DEF_GEOMETRYSCHLICKGGX = "float geometrySchlickGGX ( float NdotV , float roughness ) { float r = ( roughness + 1.0f ) ; float k = ( r * r ) / 8.0f ; float nom = NdotV ; float denom = NdotV * ( 1.0f - k ) + k ; return nom / denom ; }\n"
private const val DEF_GEOMETRYSMITH = "float geometrySmith ( vec3 N , vec3 V , vec3 L , float roughness ) { float NdotV = maxf ( dotv3 ( N , V ) , 0.0f ) ; float NdotL = maxf ( dotv3 ( N , L ) , 0.0f ) ; float ggx2 = geometrySchlickGGX ( NdotV , roughness ) ; float ggx1 = geometrySchlickGGX ( NdotL , roughness ) ; return ggx1 * ggx2 ; }\n"
private const val DEF_FRESNELSCHLICK = "vec3 fresnelSchlick ( float cosTheta , vec3 F0 ) { return addv3 ( F0 , mulv3 ( subv3 ( ftov3 ( 1.0f ) , F0 ) , ftov3 ( powf ( 1.0f - cosTheta , 5.0f ) ) ) ) ; }\n"
private const val DEF_SHADINGPBR = "vec4 shadingPbr ( vec3 eye , vec3 worldPos , vec3 albedo , vec3 N , float metallic , float roughness , float ao ) { vec3 alb = powv3 ( albedo , ftov3 ( 2.2f ) ) ; vec3 V = normv3 ( subv3 ( eye , worldPos ) ) ; vec3 F0 = ftov3 ( 0.04f ) ; F0 = mixv3 ( F0 , alb , metallic ) ; vec3 Lo = v3zero ( ) ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { vec3 toLight = subv3 ( uLights [ i ] . vector , worldPos ) ; vec3 L = normv3 ( toLight ) ; vec3 H = normv3 ( addv3 ( V , L ) ) ; float distance = lenv3 ( toLight ) ; float lum = luminosity ( distance , uLights [ i ] ) ; vec3 radiance = mulv3 ( uLights [ i ] . color , ftov3 ( lum ) ) ; float NDF = distributionGGX ( N , H , roughness ) ; float G = geometrySmith ( N , V , L , roughness ) ; vec3 F = fresnelSchlick ( maxf ( dotv3 ( H , V ) , 0.0f ) , F0 ) ; vec3 nominator = mulv3 ( F , ftov3 ( NDF * G ) ) ; float denominator = 4.0f * maxf ( dotv3 ( N , V ) , 0.0f ) * maxf ( dotv3 ( N , L ) , 0.0f ) + 0.001f ; vec3 specular = divv3f ( nominator , denominator ) ; vec3 kD = subv3 ( ftov3 ( 1.0f ) , F ) ; kD = mulv3 ( kD , ftov3 ( 1.0f - metallic ) ) ; float NdotL = maxf ( dotv3 ( N , L ) , 0.0f ) ; Lo = addv3 ( Lo , mulv3 ( mulv3 ( addv3 ( divv3 ( mulv3 ( kD , alb ) , ftov3 ( PI ) ) , specular ) , radiance ) , ftov3 ( NdotL ) ) ) ; } vec3 ambient = mulv3 ( ftov3 ( 0.1f * ao ) , alb ) ; vec3 color = addv3 ( ambient , Lo ) ; color = divv3 ( color , addv3 ( color , ftov3 ( 1.0f ) ) ) ; color = powv3 ( color , ftov3 ( 1.0f / 2.2f ) ) ; return v3tov4 ( color , 1.0f ) ; }\n"
private const val DEF_TYPE_EMPTY = "int TYPE_EMPTY = 0 ;\n"
private const val DEF_TYPE_SAND = "int TYPE_SAND = 1 ;\n"
private const val DEF_TYPE_WATER = "int TYPE_WATER = 2 ;\n"
private const val DEF_SANDCONVERT = "vec4 sandConvert ( vec4 pixel ) { if ( pixel . x > 0.9f && pixel . y > 0.9f ) { return v4 ( itof ( TYPE_SAND ) , 0.0f , 0.0f , pixel . x ) ; } if ( pixel . z > 0.9f ) { return v4 ( itof ( TYPE_WATER ) , 0.0f , 0.0f , pixel . x ) ; } else { return v4zero ( ) ; } }\n"
private const val DEF_NEARBYCELLCOORDS = "vec2 nearbyCellCoords ( vec2 uv , float cellW , float cellH , int x , int y ) { return v2 ( uv . x + itof ( x ) * cellW , uv . y + itof ( y ) * cellH ) ; }\n"
private const val DEF_TRYDEPOSITPARTICLE = "ivec2 tryDepositParticle ( sampler2D orig , vec2 uv , float cellW , float cellH , int x , int y ) { vec2 coords = nearbyCellCoords ( uv , cellW , cellH , x , y ) ; if ( coords . x < 0.0f || coords . y < 0.0f || coords . x > 1.0f || coords . y > 1.0f ) { return iv2zero ( ) ; } vec4 cell = sampler ( orig , coords ) ; if ( ftoi ( cell . x ) == TYPE_EMPTY ) { return iv2 ( x , y ) ; } return iv2zero ( ) ; }\n"
private const val DEF_SIMTYPESAND = "ivec2 simTypeSand ( sampler2D orig , vec2 uv , float cellW , float cellH ) { ivec2 deposit = tryDepositParticle ( orig , uv , cellW , cellH , 0 , - 1 ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } bool left = rndv2 ( uv ) > 0.5f ; int first = left ? - 1 : 1 ; int second = left ? 1 : - 1 ; deposit = tryDepositParticle ( orig , uv , cellW , cellH , first , - 1 ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } deposit = tryDepositParticle ( orig , uv , cellW , cellH , second , - 1 ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } return iv2zero ( ) ; }\n"
private const val DEF_SIMTYPEWATER = "ivec2 simTypeWater ( sampler2D orig , vec2 uv , float cellW , float cellH ) { ivec2 deposit = simTypeSand ( orig , uv , cellW , cellH ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } bool left = rndv3 ( v2tov3 ( uv , itof ( TYPE_WATER ) ) ) > 0.5f ; int first = left ? - 1 : 1 ; int second = left ? 1 : - 1 ; deposit = tryDepositParticle ( orig , uv , cellW , cellH , first , 0 ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } deposit = tryDepositParticle ( orig , uv , cellW , cellH , second , 0 ) ; if ( ! eqiv2 ( deposit , iv2zero ( ) ) ) { return deposit ; } return iv2zero ( ) ; }\n"
private const val DEF_SANDPHYSICS = "vec4 sandPhysics ( sampler2D orig , vec2 uv , ivec2 wh ) { float cellW = 1.0f / itof ( wh . x ) ; float cellH = 1.0f / itof ( wh . y ) ; int type = ftoi ( sampler ( orig , uv ) . x ) ; if ( type == TYPE_SAND ) { return iv2tov4 ( simTypeSand ( orig , uv , cellW , cellH ) , 0.0f , 0.0f ) ; } if ( type == TYPE_WATER ) { return iv2tov4 ( simTypeWater ( orig , uv , cellW , cellH ) , 0.0f , 0.0f ) ; } else { return v4zero ( ) ; } }\n"
private const val DEF_SANDSOLVER = "vec4 sandSolver ( sampler2D orig , sampler2D deltas , vec2 uv , ivec2 wh ) { float cellW = 1.0f / itof ( wh . x ) ; float cellH = 1.0f / itof ( wh . y ) ; vec4 result = v4zero ( ) ; for ( int x = - 1 ; x < 2 ; x ++ ) { for ( int y = - 1 ; y < 2 ; y ++ ) { vec2 coords = nearbyCellCoords ( uv , cellW , cellH , x , y ) ; if ( coords . x < 0.0f || coords . y < 0.0f || coords . x > 1.0f || coords . y > 1.0f ) { continue ; } vec4 cell = sampler ( orig , coords ) ; if ( ftoi ( cell . x ) == TYPE_EMPTY ) { continue ; } vec4 delta = sampler ( deltas , coords ) ; if ( delta . x == itof ( - x ) && delta . y == itof ( - y ) ) { return cell ; } } } return result ; }\n"
private const val DEF_SANDDRAW = "vec4 sandDraw ( sampler2D orig , vec2 uv , ivec2 wh ) { float cellW = 1.0f / itof ( wh . x ) ; float cellH = 1.0f / itof ( wh . y ) ; vec3 result = v3zero ( ) ; for ( int x = - 1 ; x < 2 ; x ++ ) { for ( int y = - 1 ; y < 2 ; y ++ ) { vec2 coords = nearbyCellCoords ( uv , cellW , cellH , x , y ) ; vec4 cell = sampler ( orig , coords ) ; int type = ftoi ( cell . x ) ; if ( type == TYPE_SAND ) { result = addv3 ( result , v3yellow ( ) ) ; } if ( type == TYPE_WATER ) { result = addv3 ( result , v3blue ( ) ) ; } else { result = addv3 ( result , mulv3f ( v3cyan ( ) , coords . y ) ) ; } } } result = divv3f ( result , 9.0f ) ; return v3tov4 ( result , 1.0f ) ; }\n"
private const val DEF_MAX_STEPS = "int MAX_STEPS = 100 ;\n"
private const val DEF_MAX_DIST = "float MAX_DIST = 100.0f ;\n"
private const val DEF_SURF_DIST = "float SURF_DIST = 0.01f ;\n"
private const val DEF_RAYMARCHERSCENE = "struct RaymarcherScene {  float cylALen ; float cylARad ; mat4 cylAMat ; vec2 coneBShape ; float coneBHeight ; mat4 coneBMat ; float cylCLen ; float cylCRad ; mat4 cylCMat ; vec3 boxDShape ; mat4 boxDMat ; vec3 boxEShape ; mat4 boxEMat ; vec2 prismFShape ; mat4 prismFMat ; float cylGLen ; float cylGRad ; mat4 cylGMat ; vec3 boxHShape ; mat4 boxHMat ;  };\n"
private const val DEF_SIMPLIDFIEDCYL = "float simplidfiedCyl ( vec3 p , float cylLen , float cylRad ) { return sdCappedCylinder ( p , v3 ( 0 , 0 , cylLen / 2.0f ) , v3 ( 0 , 0 , - cylLen / 2.0f ) , cylRad ) ; }\n"
private const val DEF_SCENEDIST = "float sceneDist ( vec3 p , RaymarcherScene scene ) { vec3 cylAP = v4tov3 ( transformv4 ( v3tov4 ( p , 1.0f ) , scene . cylAMat ) ) ; float cylA = simplidfiedCyl ( cylAP , scene . cylALen , scene . cylARad ) ; vec3 coneBP = v4tov3 ( transformv4 ( v3tov4 ( p , 1.0f ) , scene . coneBMat ) ) ; float coneB = sdCone ( coneBP , scene . coneBShape , scene . coneBHeight ) ; vec3 cylCP = v4tov3 ( transformv4 ( v3tov4 ( p , 1.0f ) , scene . cylCMat ) ) ; float cylC = simplidfiedCyl ( cylCP , scene . cylCLen , scene . cylCRad ) ; vec3 boxDP = v4tov3 ( transformv4 ( v3tov4 ( p , 1.0f ) , scene . boxDMat ) ) ; float boxD = sdBox ( boxDP , scene . boxDShape ) ; vec3 boxEP = v4tov3 ( transformv4 ( v3tov4 ( p , 1.0f ) , scene . boxEMat ) ) ; float boxE = sdBox ( boxEP , scene . boxEShape ) ; float AB = opSubtraction ( coneB , cylA ) ; float ABC = opUnion ( AB , cylC ) ; float ABCD = opUnion ( ABC , boxD ) ; float ABCDE = opUnion ( ABCD , boxE ) ; return ABCDE ; }\n"
private const val DEF_RAYMARCH = "float rayMarch ( vec3 ro , vec3 rd , RaymarcherScene scene ) { float dO = 0.0f ; for ( int i = 0 ; i < MAX_STEPS ; i ++ ) { vec3 p = addv3 ( ro , mulv3f ( rd , dO ) ) ; float dS = sceneDist ( p , scene ) ; dO += dS ; if ( dO > MAX_DIST || dS < SURF_DIST ) break ; } return dO ; }\n"
private const val DEF_GETNORMAL = "vec3 getNormal ( vec3 p , RaymarcherScene scene ) { float d = sceneDist ( p , scene ) ; vec3 n = subv3 ( ftov3 ( d ) , v3 ( sceneDist ( subv3 ( p , v3 ( 0.01f , 0.0f , 0.0f ) ) , scene ) , sceneDist ( subv3 ( p , v3 ( 0.0f , 0.01f , 0.0f ) ) , scene ) , sceneDist ( subv3 ( p , v3 ( 0.0f , 0.0f , 0.01f ) ) , scene ) ) ) ; return normv3 ( n ) ; }\n"
private const val DEF_GETLIGHT = "float getLight ( vec3 p , vec3 eye , RaymarcherScene scene ) { vec3 l = normv3 ( subv3 ( eye , p ) ) ; vec3 n = getNormal ( p , scene ) ; float dif = clampf ( dotv3 ( n , l ) , 0.0f , 1.0f ) ; float d = rayMarch ( addv3 ( p , mulv3f ( n , SURF_DIST * 2.0f ) ) , l , scene ) ; if ( d < lenv3 ( subv3 ( eye , p ) ) ) dif *= 0.1f ; return dif ; }\n"
private const val DEF_RAYMARCHER = "vec4 raymarcher ( vec3 eye , vec3 center , vec2 uv , float fovy , float aspect , ivec2 wh , int samplesAA , float cylALen , float cylARad , mat4 cylAMat , vec2 coneBShape , float coneBHeight , mat4 coneBMat , float cylCLen , float cylCRad , mat4 cylCMat , vec3 boxDShape , mat4 boxDMat , vec3 boxEShape , mat4 boxEMat , vec2 prismFShape , mat4 prismFMat , float cylGLen , float cylGRad , mat4 cylGMat , vec3 boxHShape , mat4 boxHMat ) { RaymarcherScene scene = { cylALen , cylARad , cylAMat , coneBShape , coneBHeight , coneBMat , cylCLen , cylCRad , cylCMat , boxDShape , boxDMat , boxEShape , boxEMat , prismFShape , prismFMat , cylGLen , cylGRad , cylGMat , boxHShape , boxHMat } ; Camera camera = cameraLookAt ( eye , center , v3up ( ) , fovy , aspect , 0.0f , 1.0f ) ; vec3 col = v3zero ( ) ; for ( int m = 0 ; m < samplesAA ; m ++ ) { for ( int n = 0 ; n < samplesAA ; n ++ ) { vec2 duv = divv2 ( subv2f ( divv2f ( v2 ( itof ( m ) , itof ( n ) ) , itof ( samplesAA ) ) , 0.5f ) , iv2tov2 ( wh ) ) ; ray r = rayFromCamera ( camera , addv2 ( uv , duv ) ) ; float d = rayMarch ( r . origin , r . direction , scene ) ; vec3 p = addv3 ( r . origin , mulv3f ( r . direction , d ) ) ; vec3 addition = ftov3 ( getLight ( p , eye , scene ) ) ; col = addv3 ( col , sqrtv3 ( addition ) ) ; } } col = divv3f ( col , itof ( samplesAA * samplesAA ) ) ; return v3tov4 ( col , 1.0f ) ; }\n"

const val TYPES_DEF = DEF_RAY+DEF_AABB+DEF_CAMERA+DEF_LIGHT+DEF_PHONGMATERIAL+DEF_BVHNODE+DEF_SPHERE+DEF_LAMBERTIANMATERIAL+DEF_METALLICMATERIAL+DEF_DIELECTRICMATERIAL+DEF_HITRECORD+DEF_SCATTERRESULT+DEF_REFRACTRESULT+DEF_RAYMARCHERSCENE

const val OPS_DEF = DEF_ADDF+DEF_SUBF+DEF_MULF+DEF_DIVF+DEF_EQV2+DEF_EQIV2+DEF_EQV3+DEF_EQV4+DEF_SCHLICKF+DEF_REMAPF+DEF_FTOV2+DEF_V2ZERO+DEF_ADDV2+DEF_DIVV2+DEF_DIVV2F+DEF_GETXV2+DEF_GETYV2+DEF_LENV2+DEF_INDEXV3+DEF_V2TOV3+DEF_FTOV3+DEF_V3ZERO+DEF_V3ONE+DEF_V3FRONT+DEF_V3BACK+DEF_V3LEFT+DEF_V3RIGHT+DEF_V3UP+DEF_V3DOWN+DEF_V3WHITE+DEF_V3BLACK+DEF_V3LTGREY+DEF_V3GREY+DEF_V3DKGREY+DEF_V3RED+DEF_V3GREEN+DEF_V3BLUE+DEF_V3YELLOW+DEF_V3MAGENTA+DEF_V3CYAN+DEF_V3ORANGE+DEF_V3ROSE+DEF_V3VIOLET+DEF_V3AZURE+DEF_V3AQUAMARINE+DEF_V3CHARTREUSE+DEF_XYV3+DEF_XZV3+DEF_YZV3+DEF_ABSV3+DEF_NEGV3+DEF_SUBV3F+DEF_POWV3+DEF_MIXV3+DEF_MAXV3+DEF_MINV3+DEF_LENV3+DEF_SQRTV3+DEF_LENSQV3+DEF_NORMV3+DEF_LERPV3+DEF_REFLECTV3+DEF_REFRACTV3+DEF_V3TOV4+DEF_FTOV4+DEF_V4TOV3+DEF_V4ZERO+DEF_V4ONE+DEF_ADDV4+DEF_SUBV4+DEF_MULV4+DEF_MULV4F+DEF_DIVV4+DEF_DIVV4F+DEF_GETXV4+DEF_GETYV4+DEF_GETZV4+DEF_GETWV4+DEF_GETRV4+DEF_GETGV4+DEF_GETBV4+DEF_GETAV4+DEF_SETXV4+DEF_SETYV4+DEF_SETZV4+DEF_SETWV4+DEF_SETRV4+DEF_SETGV4+DEF_SETBV4+DEF_SETAV4+DEF_IV2ZERO+DEF_IV2TOV2+DEF_IV2TOV4+DEF_GETXIV2+DEF_GETYIV2+DEF_GETUIV2+DEF_GETVIV2+DEF_TILE+DEF_RAYBACK+DEF_RAYPOINT+DEF_SDXZPLANE+DEF_SDSPHERE+DEF_SDBOX+DEF_SDCAPPEDCYLINDER+DEF_SDCONE+DEF_SDTRIPRISM+DEF_OPUNION+DEF_OPSUBTRACTION+DEF_OPINTERSECTION+DEF_RANDOMINUNITSPHERE+DEF_RANDOMINUNITDISK+DEF_CENTERUV+DEF_CAMERALOOKAT+DEF_RAYFROMCAMERA+DEF_BACKGROUND+DEF_RAYHITAABB+DEF_RAYHITSPHERERECORD+DEF_RAYHITSPHERE+DEF_RAYHITOBJECT+DEF_RAYHITBVH+DEF_RAYHITWORLD+DEF_SCATTERLAMBERTIAN+DEF_SCATTERMETALLIC+DEF_SCATTERDIELECTRIC+DEF_SCATTERMATERIAL+DEF_SAMPLECOLOR+DEF_FRAGMENTCOLORRT+DEF_GAMMASQRT+DEF_LUMINOSITY+DEF_DIFFUSECONTRIB+DEF_HALFVECTOR+DEF_SPECULARCONTRIB+DEF_LIGHTCONTRIB+DEF_POINTLIGHTCONTRIB+DEF_DIRLIGHTCONTRIB+DEF_SHADINGFLAT+DEF_SHADINGPHONG+DEF_DISTRIBUTIONGGX+DEF_GEOMETRYSCHLICKGGX+DEF_GEOMETRYSMITH+DEF_FRESNELSCHLICK+DEF_SHADINGPBR+DEF_SANDCONVERT+DEF_NEARBYCELLCOORDS+DEF_TRYDEPOSITPARTICLE+DEF_SIMTYPESAND+DEF_SIMTYPEWATER+DEF_SANDPHYSICS+DEF_SANDSOLVER+DEF_SANDDRAW+DEF_SIMPLIDFIEDCYL+DEF_SCENEDIST+DEF_RAYMARCH+DEF_GETNORMAL+DEF_GETLIGHT+DEF_RAYMARCHER

const val CONST_DEF = DEF_PI+DEF_BOUNCE_ERR+DEF_NO_HIT+DEF_NO_SCATTER+DEF_NO_REFRACT+DEF_TYPE_EMPTY+DEF_TYPE_SAND+DEF_TYPE_WATER+DEF_MAX_STEPS+DEF_MAX_DIST+DEF_SURF_DIST

fun error() = object : Expression<Float>() {
    override fun expr() = "error()"
    override fun roots() = listOf<Expression<*>>()
}

fun itof(i: Expression<Int>) = object : Expression<Float>() {
    override fun expr() = "itof(${i.expr()})"
    override fun roots() = listOf(i)
}

fun ftoi(f: Expression<Float>) = object : Expression<Int>() {
    override fun expr() = "ftoi(${f.expr()})"
    override fun roots() = listOf(f)
}

fun dtof(d: Expression<Double>) = object : Expression<Float>() {
    override fun expr() = "dtof(${d.expr()})"
    override fun roots() = listOf(d)
}

fun addf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "addf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "subf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "mulf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "divf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun eqv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<Boolean>() {
    override fun expr() = "eqv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun eqiv2(left: Expression<vec2i>, right: Expression<vec2i>) = object : Expression<Boolean>() {
    override fun expr() = "eqiv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun eqv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<Boolean>() {
    override fun expr() = "eqv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun eqv4(left: Expression<vec4>, right: Expression<vec4>) = object : Expression<Boolean>() {
    override fun expr() = "eqv4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun signf(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "signf(${value.expr()})"
    override fun roots() = listOf(value)
}

fun absf(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "absf(${value.expr()})"
    override fun roots() = listOf(value)
}

fun sqrtf(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sqrtf(${value.expr()})"
    override fun roots() = listOf(value)
}

fun sinf(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sinf(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun cosf(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "cosf(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun tanf(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "tanf(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun powf(base: Expression<Float>, power: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "powf(${base.expr()}, ${power.expr()})"
    override fun roots() = listOf(base, power)
}

fun minf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "minf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun maxf(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "maxf(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun clampf(x: Expression<Float>, lowerlimit: Expression<Float>, upperlimit: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "clampf(${x.expr()}, ${lowerlimit.expr()}, ${upperlimit.expr()})"
    override fun roots() = listOf(x, lowerlimit, upperlimit)
}

fun smoothf(edge0: Expression<Float>, edge1: Expression<Float>, x: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "smoothf(${edge0.expr()}, ${edge1.expr()}, ${x.expr()})"
    override fun roots() = listOf(edge0, edge1, x)
}

fun floorf(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "floorf(${value.expr()})"
    override fun roots() = listOf(value)
}

fun fractf(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "fractf(${value.expr()})"
    override fun roots() = listOf(value)
}

fun schlickf(cosine: Expression<Float>, ri: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "schlickf(${cosine.expr()}, ${ri.expr()})"
    override fun roots() = listOf(cosine, ri)
}

fun remapf(a: Expression<Float>, b: Expression<Float>, c: Expression<Float>, d: Expression<Float>, t: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "remapf(${a.expr()}, ${b.expr()}, ${c.expr()}, ${d.expr()}, ${t.expr()})"
    override fun roots() = listOf(a, b, c, d, t)
}

fun v2(x: Expression<Float>, y: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "v2(${x.expr()}, ${y.expr()})"
    override fun roots() = listOf(x, y)
}

fun ftov2(v: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "ftov2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun v2zero() = object : Expression<vec2>() {
    override fun expr() = "v2zero()"
    override fun roots() = listOf<Expression<*>>()
}

fun addv2(l: Expression<vec2>, r: Expression<vec2>) = object : Expression<vec2>() {
    override fun expr() = "addv2(${l.expr()}, ${r.expr()})"
    override fun roots() = listOf(l, r)
}

fun subv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<vec2>() {
    override fun expr() = "subv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<vec2>() {
    override fun expr() = "mulv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<vec2>() {
    override fun expr() = "divv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv2f(vec: Expression<vec2>, v: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "mulv2f(${vec.expr()}, ${v.expr()})"
    override fun roots() = listOf(vec, v)
}

fun divv2f(v: Expression<vec2>, f: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "divv2f(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun addv2f(left: Expression<vec2>, right: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "addv2f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subv2f(left: Expression<vec2>, right: Expression<Float>) = object : Expression<vec2>() {
    override fun expr() = "subv2f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun dotv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "dotv2(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun getxv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getxv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getyv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getyv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun lenv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "lenv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun indexv3(v: Expression<vec3>, index: Expression<Int>) = object : Expression<Float>() {
    override fun expr() = "indexv3(${v.expr()}, ${index.expr()})"
    override fun roots() = listOf(v, index)
}

fun v3(x: Expression<Float>, y: Expression<Float>, z: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "v3(${x.expr()}, ${y.expr()}, ${z.expr()})"
    override fun roots() = listOf(x, y, z)
}

fun v2tov3(v: Expression<vec2>, f: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "v2tov3(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun ftov3(v: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "ftov3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun v3zero() = object : Expression<vec3>() {
    override fun expr() = "v3zero()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3one() = object : Expression<vec3>() {
    override fun expr() = "v3one()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3front() = object : Expression<vec3>() {
    override fun expr() = "v3front()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3back() = object : Expression<vec3>() {
    override fun expr() = "v3back()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3left() = object : Expression<vec3>() {
    override fun expr() = "v3left()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3right() = object : Expression<vec3>() {
    override fun expr() = "v3right()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3up() = object : Expression<vec3>() {
    override fun expr() = "v3up()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3down() = object : Expression<vec3>() {
    override fun expr() = "v3down()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3white() = object : Expression<vec3>() {
    override fun expr() = "v3white()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3black() = object : Expression<vec3>() {
    override fun expr() = "v3black()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3ltGrey() = object : Expression<vec3>() {
    override fun expr() = "v3ltGrey()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3grey() = object : Expression<vec3>() {
    override fun expr() = "v3grey()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3dkGrey() = object : Expression<vec3>() {
    override fun expr() = "v3dkGrey()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3red() = object : Expression<vec3>() {
    override fun expr() = "v3red()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3green() = object : Expression<vec3>() {
    override fun expr() = "v3green()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3blue() = object : Expression<vec3>() {
    override fun expr() = "v3blue()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3yellow() = object : Expression<vec3>() {
    override fun expr() = "v3yellow()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3magenta() = object : Expression<vec3>() {
    override fun expr() = "v3magenta()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3cyan() = object : Expression<vec3>() {
    override fun expr() = "v3cyan()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3orange() = object : Expression<vec3>() {
    override fun expr() = "v3orange()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3rose() = object : Expression<vec3>() {
    override fun expr() = "v3rose()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3violet() = object : Expression<vec3>() {
    override fun expr() = "v3violet()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3azure() = object : Expression<vec3>() {
    override fun expr() = "v3azure()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3aquamarine() = object : Expression<vec3>() {
    override fun expr() = "v3aquamarine()"
    override fun roots() = listOf<Expression<*>>()
}

fun v3chartreuse() = object : Expression<vec3>() {
    override fun expr() = "v3chartreuse()"
    override fun roots() = listOf<Expression<*>>()
}

fun xyv3(vec: Expression<vec3>) = object : Expression<vec2>() {
    override fun expr() = "xyv3(${vec.expr()})"
    override fun roots() = listOf(vec)
}

fun xzv3(vec: Expression<vec3>) = object : Expression<vec2>() {
    override fun expr() = "xzv3(${vec.expr()})"
    override fun roots() = listOf(vec)
}

fun yzv3(vec: Expression<vec3>) = object : Expression<vec2>() {
    override fun expr() = "yzv3(${vec.expr()})"
    override fun roots() = listOf(vec)
}

fun absv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "absv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun negv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "negv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun dotv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "dotv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun crossv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "crossv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun addv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "addv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "subv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "subv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "mulv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "mulv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "divv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "divv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun powv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "powv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mixv3(left: Expression<vec3>, right: Expression<vec3>, proportion: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "mixv3(${left.expr()}, ${right.expr()}, ${proportion.expr()})"
    override fun roots() = listOf(left, right, proportion)
}

fun maxv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "maxv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun minv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "minv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun lenv3(v: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "lenv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun sqrtv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "sqrtv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun lensqv3(v: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "lensqv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun normv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "normv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun lerpv3(from: Expression<vec3>, to: Expression<vec3>, t: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "lerpv3(${from.expr()}, ${to.expr()}, ${t.expr()})"
    override fun roots() = listOf(from, to, t)
}

fun reflectv3(v: Expression<vec3>, n: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "reflectv3(${v.expr()}, ${n.expr()})"
    override fun roots() = listOf(v, n)
}

fun v4(x: Expression<Float>, y: Expression<Float>, z: Expression<Float>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "v4(${x.expr()}, ${y.expr()}, ${z.expr()}, ${w.expr()})"
    override fun roots() = listOf(x, y, z, w)
}

fun v3tov4(v: Expression<vec3>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "v3tov4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun ftov4(v: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "ftov4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun v4tov3(v: Expression<vec4>) = object : Expression<vec3>() {
    override fun expr() = "v4tov3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun v4zero() = object : Expression<vec4>() {
    override fun expr() = "v4zero()"
    override fun roots() = listOf<Expression<*>>()
}

fun v4one() = object : Expression<vec4>() {
    override fun expr() = "v4one()"
    override fun roots() = listOf<Expression<*>>()
}

fun addv4(left: Expression<vec4>, right: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "addv4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun subv4(left: Expression<vec4>, right: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "subv4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv4(left: Expression<vec4>, right: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "mulv4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv4f(left: Expression<vec4>, right: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "mulv4f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv4(left: Expression<vec4>, right: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "divv4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun divv4f(left: Expression<vec4>, right: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "divv4f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun getxv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getxv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getyv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getyv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getzv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getzv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getwv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getwv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getrv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getrv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getgv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getgv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getbv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getbv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getav4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "getav4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun setxv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setxv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setyv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setyv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setzv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setzv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setwv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setwv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setrv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setrv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setgv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setgv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setbv4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setbv4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun setav4(v: Expression<vec4>, f: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "setav4(${v.expr()}, ${f.expr()})"
    override fun roots() = listOf(v, f)
}

fun iv2(x: Expression<Int>, y: Expression<Int>) = object : Expression<vec2i>() {
    override fun expr() = "iv2(${x.expr()}, ${y.expr()})"
    override fun roots() = listOf(x, y)
}

fun iv2zero() = object : Expression<vec2i>() {
    override fun expr() = "iv2zero()"
    override fun roots() = listOf<Expression<*>>()
}

fun iv2tov2(v: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "iv2tov2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun iv2tov4(vec: Expression<vec2i>, z: Expression<Float>, w: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "iv2tov4(${vec.expr()}, ${z.expr()}, ${w.expr()})"
    override fun roots() = listOf(vec, z, w)
}

fun getxiv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getxiv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getyiv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getyiv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getuiv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getuiv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getviv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getviv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"
    override fun roots() = listOf(texCoord, uv, cnt)
}

fun scalem2(scale: Expression<vec2>) = object : Expression<mat2>() {
    override fun expr() = "scalem2(${scale.expr()})"
    override fun roots() = listOf(scale)
}

fun transformv2(vec: Expression<vec2>, mat: Expression<mat2>) = object : Expression<vec2>() {
    override fun expr() = "transformv2(${vec.expr()}, ${mat.expr()})"
    override fun roots() = listOf(vec, mat)
}

fun m4ident() = object : Expression<mat4>() {
    override fun expr() = "m4ident()"
    override fun roots() = listOf<Expression<*>>()
}

fun mulm4(left: Expression<mat4>, right: Expression<mat4>) = object : Expression<mat4>() {
    override fun expr() = "mulm4(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun transformv4(vec: Expression<vec4>, mat: Expression<mat4>) = object : Expression<vec4>() {
    override fun expr() = "transformv4(${vec.expr()}, ${mat.expr()})"
    override fun roots() = listOf(vec, mat)
}

fun translatem4(vec: Expression<vec3>) = object : Expression<mat4>() {
    override fun expr() = "translatem4(${vec.expr()})"
    override fun roots() = listOf(vec)
}

fun rotatem4(axis: Expression<vec3>, angle: Expression<Float>) = object : Expression<mat4>() {
    override fun expr() = "rotatem4(${axis.expr()}, ${angle.expr()})"
    override fun roots() = listOf(axis, angle)
}

fun scalem4(scale: Expression<vec3>) = object : Expression<mat4>() {
    override fun expr() = "scalem4(${scale.expr()})"
    override fun roots() = listOf(scale)
}

fun rayBack() = object : Expression<ray>() {
    override fun expr() = "rayBack()"
    override fun roots() = listOf<Expression<*>>()
}

fun rayPoint(ray: Expression<ray>, t: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "rayPoint(${ray.expr()}, ${t.expr()})"
    override fun roots() = listOf(ray, t)
}

fun sdXZPlane(p: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "sdXZPlane(${p.expr()})"
    override fun roots() = listOf(p)
}

fun sdSphere(p: Expression<vec3>, r: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sdSphere(${p.expr()}, ${r.expr()})"
    override fun roots() = listOf(p, r)
}

fun sdBox(p: Expression<vec3>, b: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "sdBox(${p.expr()}, ${b.expr()})"
    override fun roots() = listOf(p, b)
}

fun sdCappedCylinder(p: Expression<vec3>, a: Expression<vec3>, b: Expression<vec3>, r: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sdCappedCylinder(${p.expr()}, ${a.expr()}, ${b.expr()}, ${r.expr()})"
    override fun roots() = listOf(p, a, b, r)
}

fun sdCone(p: Expression<vec3>, c: Expression<vec2>, h: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sdCone(${p.expr()}, ${c.expr()}, ${h.expr()})"
    override fun roots() = listOf(p, c, h)
}

fun sdTriPrism(p: Expression<vec3>, h: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "sdTriPrism(${p.expr()}, ${h.expr()})"
    override fun roots() = listOf(p, h)
}

fun opUnion(d1: Expression<Float>, d2: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "opUnion(${d1.expr()}, ${d2.expr()})"
    override fun roots() = listOf(d1, d2)
}

fun opSubtraction(d1: Expression<Float>, d2: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "opSubtraction(${d1.expr()}, ${d2.expr()})"
    override fun roots() = listOf(d1, d2)
}

fun opIntersection(d1: Expression<Float>, d2: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "opIntersection(${d1.expr()}, ${d2.expr()})"
    override fun roots() = listOf(d1, d2)
}

fun rndf(x: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "rndf(${x.expr()})"
    override fun roots() = listOf(x)
}

fun rndv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "rndv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun rndv3(v: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "rndv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun rndv4(v: Expression<vec4>) = object : Expression<Float>() {
    override fun expr() = "rndv4(${v.expr()})"
    override fun roots() = listOf(v)
}

fun seedRandom(s: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "seedRandom(${s.expr()})"
    override fun roots() = listOf(s)
}

fun seededRndf() = object : Expression<Float>() {
    override fun expr() = "seededRndf()"
    override fun roots() = listOf<Expression<*>>()
}

fun randomInUnitSphere() = object : Expression<vec3>() {
    override fun expr() = "randomInUnitSphere()"
    override fun roots() = listOf<Expression<*>>()
}

fun randomInUnitDisk() = object : Expression<vec3>() {
    override fun expr() = "randomInUnitDisk()"
    override fun roots() = listOf<Expression<*>>()
}

fun sampler(sampler: Expression<GlTexture>, texCoords: Expression<vec2>) = object : Expression<vec4>() {
    override fun expr() = "sampler(${sampler.expr()}, ${texCoords.expr()})"
    override fun roots() = listOf(sampler, texCoords)
}

fun texel(sampler: Expression<GlTexture>, index: Expression<Int>) = object : Expression<vec4>() {
    override fun expr() = "texel(${sampler.expr()}, ${index.expr()})"
    override fun roots() = listOf(sampler, index)
}

fun samplerq(sampler: Expression<GlTexture>, texCoords: Expression<vec3>) = object : Expression<vec4>() {
    override fun expr() = "samplerq(${sampler.expr()}, ${texCoords.expr()})"
    override fun roots() = listOf(sampler, texCoords)
}

fun fragmentColorRt(width: Expression<Int>, height: Expression<Int>, random: Expression<Float>, sampleCnt: Expression<Int>, rayBounces: Expression<Int>, eye: Expression<vec3>, center: Expression<vec3>, up: Expression<vec3>, fovy: Expression<Float>, aspect: Expression<Float>, aperture: Expression<Float>, focusDist: Expression<Float>, texCoord: Expression<vec2>) = object : Expression<vec4>() {
    override fun expr() = "fragmentColorRt(${width.expr()}, ${height.expr()}, ${random.expr()}, ${sampleCnt.expr()}, ${rayBounces.expr()}, ${eye.expr()}, ${center.expr()}, ${up.expr()}, ${fovy.expr()}, ${aspect.expr()}, ${aperture.expr()}, ${focusDist.expr()}, ${texCoord.expr()})"
    override fun roots() = listOf(width, height, random, sampleCnt, rayBounces, eye, center, up, fovy, aspect, aperture, focusDist, texCoord)
}

fun gammaSqrt(result: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "gammaSqrt(${result.expr()})"
    override fun roots() = listOf(result)
}

fun luminosity(distance: Expression<Float>, light: Expression<Light>) = object : Expression<Float>() {
    override fun expr() = "luminosity(${distance.expr()}, ${light.expr()})"
    override fun roots() = listOf(distance, light)
}

fun diffuseContrib(lightDir: Expression<vec3>, fragNormal: Expression<vec3>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "diffuseContrib(${lightDir.expr()}, ${fragNormal.expr()}, ${material.expr()})"
    override fun roots() = listOf(lightDir, fragNormal, material)
}

fun halfVector(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "halfVector(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun specularContrib(viewDir: Expression<vec3>, lightDir: Expression<vec3>, fragNormal: Expression<vec3>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "specularContrib(${viewDir.expr()}, ${lightDir.expr()}, ${fragNormal.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, lightDir, fragNormal, material)
}

fun lightContrib(viewDir: Expression<vec3>, lightDir: Expression<vec3>, fragNormal: Expression<vec3>, attenuation: Expression<Float>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "lightContrib(${viewDir.expr()}, ${lightDir.expr()}, ${fragNormal.expr()}, ${attenuation.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, lightDir, fragNormal, attenuation, light, material)
}

fun pointLightContrib(viewDir: Expression<vec3>, fragPosition: Expression<vec3>, fragNormal: Expression<vec3>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "pointLightContrib(${viewDir.expr()}, ${fragPosition.expr()}, ${fragNormal.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, fragPosition, fragNormal, light, material)
}

fun dirLightContrib(viewDir: Expression<vec3>, fragNormal: Expression<vec3>, light: Expression<Light>, material: Expression<PhongMaterial>) = object : Expression<vec3>() {
    override fun expr() = "dirLightContrib(${viewDir.expr()}, ${fragNormal.expr()}, ${light.expr()}, ${material.expr()})"
    override fun roots() = listOf(viewDir, fragNormal, light, material)
}

fun shadingFlat(color: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "shadingFlat(${color.expr()})"
    override fun roots() = listOf(color)
}

fun shadingPhong(fragPosition: Expression<vec3>, eye: Expression<vec3>, fragNormal: Expression<vec3>, fragAlbedo: Expression<vec3>, material: Expression<PhongMaterial>) = object : Expression<vec4>() {
    override fun expr() = "shadingPhong(${fragPosition.expr()}, ${eye.expr()}, ${fragNormal.expr()}, ${fragAlbedo.expr()}, ${material.expr()})"
    override fun roots() = listOf(fragPosition, eye, fragNormal, fragAlbedo, material)
}

fun getNormalFromMap(normal: Expression<vec3>, worldPos: Expression<vec3>, texCoord: Expression<vec2>, vnormal: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "getNormalFromMap(${normal.expr()}, ${worldPos.expr()}, ${texCoord.expr()}, ${vnormal.expr()})"
    override fun roots() = listOf(normal, worldPos, texCoord, vnormal)
}

fun distributionGGX(N: Expression<vec3>, H: Expression<vec3>, a: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "distributionGGX(${N.expr()}, ${H.expr()}, ${a.expr()})"
    override fun roots() = listOf(N, H, a)
}

fun geometrySchlickGGX(NdotV: Expression<Float>, roughness: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "geometrySchlickGGX(${NdotV.expr()}, ${roughness.expr()})"
    override fun roots() = listOf(NdotV, roughness)
}

fun geometrySmith(N: Expression<vec3>, V: Expression<vec3>, L: Expression<vec3>, roughness: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "geometrySmith(${N.expr()}, ${V.expr()}, ${L.expr()}, ${roughness.expr()})"
    override fun roots() = listOf(N, V, L, roughness)
}

fun fresnelSchlick(cosTheta: Expression<Float>, F0: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "fresnelSchlick(${cosTheta.expr()}, ${F0.expr()})"
    override fun roots() = listOf(cosTheta, F0)
}

fun shadingPbr(eye: Expression<vec3>, worldPos: Expression<vec3>, albedo: Expression<vec3>, N: Expression<vec3>, metallic: Expression<Float>, roughness: Expression<Float>, ao: Expression<Float>) = object : Expression<vec4>() {
    override fun expr() = "shadingPbr(${eye.expr()}, ${worldPos.expr()}, ${albedo.expr()}, ${N.expr()}, ${metallic.expr()}, ${roughness.expr()}, ${ao.expr()})"
    override fun roots() = listOf(eye, worldPos, albedo, N, metallic, roughness, ao)
}

fun sandConvert(pixel: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "sandConvert(${pixel.expr()})"
    override fun roots() = listOf(pixel)
}

fun sandPhysics(orig: Expression<GlTexture>, uv: Expression<vec2>, wh: Expression<vec2i>) = object : Expression<vec4>() {
    override fun expr() = "sandPhysics(${orig.expr()}, ${uv.expr()}, ${wh.expr()})"
    override fun roots() = listOf(orig, uv, wh)
}

fun sandSolver(orig: Expression<GlTexture>, deltas: Expression<GlTexture>, uv: Expression<vec2>, wh: Expression<vec2i>) = object : Expression<vec4>() {
    override fun expr() = "sandSolver(${orig.expr()}, ${deltas.expr()}, ${uv.expr()}, ${wh.expr()})"
    override fun roots() = listOf(orig, deltas, uv, wh)
}

fun sandDraw(orig: Expression<GlTexture>, uv: Expression<vec2>, wh: Expression<vec2i>) = object : Expression<vec4>() {
    override fun expr() = "sandDraw(${orig.expr()}, ${uv.expr()}, ${wh.expr()})"
    override fun roots() = listOf(orig, uv, wh)
}

fun raymarcher(eye: Expression<vec3>, center: Expression<vec3>, uv: Expression<vec2>, fovy: Expression<Float>, aspect: Expression<Float>, wh: Expression<vec2i>, samplesAA: Expression<Int>, cylALen: Expression<Float>, cylARad: Expression<Float>, cylAMat: Expression<mat4>, coneBShape: Expression<vec2>, coneBHeight: Expression<Float>, coneBMat: Expression<mat4>, cylCLen: Expression<Float>, cylCRad: Expression<Float>, cylCMat: Expression<mat4>, boxDShape: Expression<vec3>, boxDMat: Expression<mat4>, boxEShape: Expression<vec3>, boxEMat: Expression<mat4>, prismFShape: Expression<vec2>, prismFMat: Expression<mat4>, cylGLen: Expression<Float>, cylGRad: Expression<Float>, cylGMat: Expression<mat4>, boxHShape: Expression<vec3>, boxHMat: Expression<mat4>) = object : Expression<vec4>() {
    override fun expr() = "raymarcher(${eye.expr()}, ${center.expr()}, ${uv.expr()}, ${fovy.expr()}, ${aspect.expr()}, ${wh.expr()}, ${samplesAA.expr()}, ${cylALen.expr()}, ${cylARad.expr()}, ${cylAMat.expr()}, ${coneBShape.expr()}, ${coneBHeight.expr()}, ${coneBMat.expr()}, ${cylCLen.expr()}, ${cylCRad.expr()}, ${cylCMat.expr()}, ${boxDShape.expr()}, ${boxDMat.expr()}, ${boxEShape.expr()}, ${boxEMat.expr()}, ${prismFShape.expr()}, ${prismFMat.expr()}, ${cylGLen.expr()}, ${cylGRad.expr()}, ${cylGMat.expr()}, ${boxHShape.expr()}, ${boxHMat.expr()})"
    override fun roots() = listOf(eye, center, uv, fovy, aspect, wh, samplesAA, cylALen, cylARad, cylAMat, coneBShape, coneBHeight, coneBMat, cylCLen, cylCRad, cylCMat, boxDShape, boxDMat, boxEShape, boxEMat, prismFShape, prismFMat, cylGLen, cylGRad, cylGMat, boxHShape, boxHMat)
}


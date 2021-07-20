package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.PhongMaterial

private const val DEF_RAY = "struct Ray {  vec3 origin ; vec3 direction ;  };\n"
private const val DEF_AABB = "struct AABB {  vec3 pointMin ; vec3 pointMax ;  };\n"
private const val DEF_RTCAMERA = "struct RtCamera {  vec3 origin ; vec3 lowerLeft ; vec3 horizontal ; vec3 vertical ; vec3 w , u , v ; float lensRadius ;  };\n"
private const val DEF_LIGHT = "struct Light {  vec3 vector ; vec3 color ; float attenConstant ; float attenLinear ; float attenQuadratic ;  };\n"
private const val DEF_PHONGMATERIAL = "struct PhongMaterial {  vec3 ambient ; vec3 diffuse ; vec3 specular ; float shine ; float transparency ;  };\n"
private const val DEF_BVHNODE = "struct BvhNode {  AABB aabb ; int leftType ; int leftIndex ; int rightType ; int rightIndex ;  };\n"
private const val DEF_SPHERE = "struct Sphere {  vec3 center ; float radius ; int materialType ; int materialIndex ;  };\n"
private const val DEF_LAMBERTIANMATERIAL = "struct LambertianMaterial {  vec3 albedo ;  };\n"
private const val DEF_METALLICMATERIAL = "struct MetallicMaterial {  vec3 albedo ;  };\n"
private const val DEF_DIELECTRICMATERIAL = "struct DielectricMaterial {  float reflectiveIndex ;  };\n"
private const val DEF_HITRECORD = "struct HitRecord {  float t ; vec3 point ; vec3 normal ; int materialType ; int materialIndex ;  };\n"
private const val DEF_SCATTERRESULT = "struct ScatterResult {  vec3 attenuation ; Ray scattered ;  };\n"
private const val DEF_REFRACTRESULT = "struct RefractResult {  bool isRefracted ; vec3 refracted ;  };\n"
private const val DEF_PI = "float PI = 3.14159265359f ;\n"
private const val DEF_BOUNCE_ERR = "float BOUNCE_ERR = 0.001f ;\n"
private const val DEF_NO_HIT = "HitRecord NO_HIT = { - 1 , { 0 , 0 , 0 } , { 1 , 0 , 0 } , 0 , 0 } ;\n"
private const val DEF_NO_SCATTER = "ScatterResult NO_SCATTER = { { - 1 , - 1 , - 1 } , { { 0 , 0 , 0 } , { 0 , 0 , 0 } } } ;\n"
private const val DEF_NO_REFRACT = "RefractResult NO_REFRACT = { false , { 0 , 0 , 0 } } ;\n"
private const val DEF_FLAGERROR = "int flagError ( ) { errorFlag = true ; return 1 ; }\n"
private const val DEF_FTOV2 = "vec2 ftov2 ( float v ) { return v2 ( v , v ) ; }\n"
private const val DEF_V2ZERO = "vec2 v2zero ( ) { return ftov2 ( 0.0f ) ; }\n"
private const val DEF_GETXV2 = "float getxv2 ( vec2 v ) { return v . x ; }\n"
private const val DEF_GETYV2 = "float getyv2 ( vec2 v ) { return v . y ; }\n"
private const val DEF_GETUV2 = "float getuv2 ( vec2 v ) { return v . x ; }\n"
private const val DEF_GETVV2 = "float getvv2 ( vec2 v ) { return v . y ; }\n"
private const val DEF_INDEXV3 = "float indexv3 ( vec3 v , int index ) { switch ( index ) { case 0 : return v . x ; case 1 : return v . y ; case 2 : return v . z ; default : flagError ( ) ; return v . x ; } }\n"
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
private const val DEF_V3TOV4 = "vec4 v3tov4 ( vec3 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n"
private const val DEF_FTOV4 = "vec4 ftov4 ( float v ) { return v4 ( v , v , v , v ) ; }\n"
private const val DEF_V4ZERO = "vec4 v4zero ( ) { return ftov4 ( 0.0f ) ; }\n"
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
private const val DEF_RAYBACK = "Ray rayBack ( ) { Ray result = { v3zero ( ) , v3back ( ) } ; return result ; }\n"
private const val DEF_EQV2 = "bool eqv2 ( vec2 left , vec2 right ) { return left . x == right . x && left . y == right . y ; }\n"
private const val DEF_EQV3 = "bool eqv3 ( vec3 left , vec3 right ) { return left . x == right . x && left . y == right . y && left . z == right . z ; }\n"
private const val DEF_EQV4 = "bool eqv4 ( vec4 left , vec4 right ) { return left . x == right . x && left . y == right . y && left . z == right . z && left . w == right . w ; }\n"
private const val DEF_SQRTV = "float sqrtv ( float value ) { return sqrt ( value ) ; }\n"
private const val DEF_SINV = "float sinv ( float rad ) { return sin ( rad ) ; }\n"
private const val DEF_COSV = "float cosv ( float rad ) { return cos ( rad ) ; }\n"
private const val DEF_TANV = "float tanv ( float rad ) { return tan ( rad ) ; }\n"
private const val DEF_POWV = "float powv ( float base , float power ) { return pow ( base , power ) ; }\n"
private const val DEF_MINV = "float minv ( float left , float right ) { return min ( left , right ) ; }\n"
private const val DEF_MAXV = "float maxv ( float left , float right ) { return max ( left , right ) ; }\n"
private const val DEF_NEGV3 = "vec3 negv3 ( vec3 v ) { return v3 ( - v . x , - v . y , - v . z ) ; }\n"
private const val DEF_ADDF = "float addf ( float left , float right ) { return left + right ; }\n"
private const val DEF_SUBF = "float subf ( float left , float right ) { return left - right ; }\n"
private const val DEF_MULF = "float mulf ( float left , float right ) { return left * right ; }\n"
private const val DEF_DIVF = "float divf ( float left , float right ) { return left / right ; }\n"
private const val DEF_POWV3 = "vec3 powv3 ( vec3 left , vec3 right ) { return v3 ( pow ( left . x , right . x ) , pow ( left . y , right . y ) , pow ( left . z , right . z ) ) ; }\n"
private const val DEF_MIXV3 = "vec3 mixv3 ( vec3 left , vec3 right , float proportion ) { return addv3 ( mulv3 ( left , ftov3 ( 1.0f - proportion ) ) , mulv3 ( right , ftov3 ( proportion ) ) ) ; }\n"
private const val DEF_ADDV4 = "vec4 addv4 ( vec4 left , vec4 right ) { return v4 ( left . x + right . x , left . y + right . y , left . z + right . z , left . w + right . w ) ; }\n"
private const val DEF_SUBV4 = "vec4 subv4 ( vec4 left , vec4 right ) { return v4 ( left . x - right . x , left . y - right . y , left . z - right . z , left . w - right . w ) ; }\n"
private const val DEF_MULV4 = "vec4 mulv4 ( vec4 left , vec4 right ) { return v4 ( left . x * right . x , left . y * right . y , left . z * right . z , left . w * right . w ) ; }\n"
private const val DEF_MULV4F = "vec4 mulv4f ( vec4 left , float right ) { return v4 ( left . x * right , left . y * right , left . z * right , left . w * right ) ; }\n"
private const val DEF_DIVV4 = "vec4 divv4 ( vec4 left , vec4 right ) { return v4 ( left . x / right . x , left . y / right . y , left . z / right . z , left . w / right . w ) ; }\n"
private const val DEF_DIVV4F = "vec4 divv4f ( vec4 left , float right ) { return v4 ( left . x / right , left . y / right , left . z / right , left . z / right ) ; }\n"
private const val DEF_LENV3 = "float lenv3 ( vec3 v ) { return sqrt ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n"
private const val DEF_LENSQV3 = "float lensqv3 ( vec3 v ) { return ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n"
private const val DEF_NORMV3 = "vec3 normv3 ( vec3 v ) { return divv3f ( v , lenv3 ( v ) ) ; }\n"
private const val DEF_LERPV3 = "vec3 lerpv3 ( vec3 from , vec3 to , float t ) { return addv3 ( mulv3f ( from , 1.0f - t ) , mulv3f ( to , t ) ) ; }\n"
private const val DEF_RAYPOINT = "vec3 rayPoint ( Ray ray , float t ) { return addv3 ( ray . origin , mulv3f ( ray . direction , t ) ) ; }\n"
private const val DEF_SCHLICK = "float schlick ( float cosine , float ri ) { float r0 = ( 1 - ri ) / ( 1 + ri ) ; r0 = r0 * r0 ; return r0 + ( 1 - r0 ) * pow ( ( 1 - cosine ) , 5 ) ; }\n"
private const val DEF_REFLECTV3 = "vec3 reflectv3 ( vec3 v , vec3 n ) { return subv3 ( v , mulv3f ( n , 2.0f * dotv3 ( v , n ) ) ) ; }\n"
private const val DEF_REFRACTV3 = "RefractResult refractv3 ( vec3 v , vec3 n , float niOverNt ) { vec3 unitV = normv3 ( v ) ; float dt = dotv3 ( unitV , n ) ; float D = 1.0f - niOverNt * niOverNt * ( 1.0f - dt * dt ) ; if ( D > 0 ) { vec3 left = mulv3f ( subv3 ( unitV , mulv3f ( n , dt ) ) , niOverNt ) ; vec3 right = mulv3f ( n , sqrt ( D ) ) ; RefractResult result = { true , subv3 ( left , right ) } ; return result ; } else { return NO_REFRACT ; } }\n"
private const val DEF_RANDOMINUNITSPHERE = "vec3 randomInUnitSphere ( ) { vec3 result ; for ( int i = 0 ; i < 10 ; i ++ ) { result = v3 ( seededRndf ( ) * 2.0f - 1.0f , seededRndf ( ) * 2.0f - 1.0f , seededRndf ( ) * 2.0f - 1.0f ) ; if ( lensqv3 ( result ) >= 1.0f ) { return result ; } } return normv3 ( result ) ; }\n"
private const val DEF_RANDOMINUNITDISK = "vec3 randomInUnitDisk ( ) { vec3 result ; for ( int i = 0 ; i < 10 ; i ++ ) { result = subv3 ( mulv3f ( v3 ( seededRndf ( ) , seededRndf ( ) , 0.0f ) , 2.0f ) , v3 ( 1.0f , 1.0f , 0.0f ) ) ; if ( dotv3 ( result , result ) >= 1.0f ) { return result ; } } return normv3 ( result ) ; }\n"
private const val DEF_ERRORHANDLER = "vec4 errorHandler ( vec4 color ) { if ( errorFlag ) { vec3 signal ; float check = seededRndf ( ) ; if ( check > 0.6f ) { signal = v3red ( ) ; } else if ( check > 0.3f ) { signal = v3blue ( ) ; } else { signal = v3green ( ) ; } return v3tov4 ( signal , 1.0f ) ; } else { return color ; } }\n"
private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; return v2 ( tileStartX + texCoord . x * tileSideX , tileStartY + texCoord . y * tileSideY ) ; }\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n"
private const val DEF_DIFFUSECONTRIB = "vec3 diffuseContrib ( vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { float diffuseTerm = dotv3 ( fragNormal , lightDir ) ; return diffuseTerm > 0.0f ? mulv3f ( material . diffuse , diffuseTerm ) : v3zero ( ) ; }\n"
private const val DEF_HALFVECTOR = "vec3 halfVector ( vec3 left , vec3 right ) { return normv3 ( addv3 ( left , right ) ) ; }\n"
private const val DEF_SPECULARCONTRIB = "vec3 specularContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { vec3 hv = halfVector ( viewDir , lightDir ) ; float specularTerm = dotv3 ( hv , fragNormal ) ; return specularTerm > 0.0f ? mulv3f ( material . specular , pow ( specularTerm , material . shine ) ) : v3zero ( ) ; }\n"
private const val DEF_LIGHTCONTRIB = "vec3 lightContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , float attenuation , Light light , PhongMaterial material ) { vec3 lighting = v3zero ( ) ; lighting = addv3 ( lighting , diffuseContrib ( lightDir , fragNormal , material ) ) ; lighting = addv3 ( lighting , specularContrib ( viewDir , lightDir , fragNormal , material ) ) ; return mulv3 ( mulv3f ( light . color , attenuation ) , lighting ) ; }\n"
private const val DEF_POINTLIGHTCONTRIB = "vec3 pointLightContrib ( vec3 viewDir , vec3 fragPosition , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 direction = subv3 ( light . vector , fragPosition ) ; vec3 lightDir = normv3 ( direction ) ; if ( dotv3 ( lightDir , fragNormal ) < 0.0f ) { return v3zero ( ) ; } float distance = lenv3 ( direction ) ; float lum = luminosity ( distance , light ) ; return lightContrib ( viewDir , lightDir , fragNormal , lum , light , material ) ; }\n"
private const val DEF_DIRLIGHTCONTRIB = "vec3 dirLightContrib ( vec3 viewDir , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 lightDir = negv3 ( normv3 ( light . vector ) ) ; return lightContrib ( viewDir , lightDir , fragNormal , 1.0f , light , material ) ; }\n"
private const val DEF_SHADINGFLAT = "vec4 shadingFlat ( vec4 color ) { return color ; }\n"
private const val DEF_SHADINGPHONG = "vec4 shadingPhong ( vec3 fragPosition , vec3 eye , vec3 fragNormal , vec3 fragAlbedo , PhongMaterial material ) { vec3 viewDir = normv3 ( subv3 ( eye , fragPosition ) ) ; vec3 color = material . ambient ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { color = addv3 ( color , pointLightContrib ( viewDir , fragPosition , fragNormal , uLights [ i ] , material ) ) ; } for ( int i = uLightsPointCnt ; i < uLightsPointCnt + uLightsDirCnt ; ++ i ) { color = addv3 ( color , dirLightContrib ( viewDir , fragNormal , uLights [ i ] , material ) ) ; } color = mulv3 ( color , fragAlbedo ) ; return v3tov4 ( color , material . transparency ) ; }\n"
private const val DEF_DISTRIBUTIONGGX = "float distributionGGX ( vec3 N , vec3 H , float a ) { float a2 = a * a ; float NdotH = max ( dotv3 ( N , H ) , 0.0f ) ; float NdotH2 = NdotH * NdotH ; float nom = a2 ; float denom = ( NdotH2 * ( a2 - 1.0f ) + 1.0f ) ; denom = PI * denom * denom ; return nom / denom ; }\n"
private const val DEF_GEOMETRYSCHLICKGGX = "float geometrySchlickGGX ( float NdotV , float roughness ) { float r = ( roughness + 1.0f ) ; float k = ( r * r ) / 8.0f ; float nom = NdotV ; float denom = NdotV * ( 1.0f - k ) + k ; return nom / denom ; }\n"
private const val DEF_GEOMETRYSMITH = "float geometrySmith ( vec3 N , vec3 V , vec3 L , float roughness ) { float NdotV = max ( dotv3 ( N , V ) , 0.0f ) ; float NdotL = max ( dotv3 ( N , L ) , 0.0f ) ; float ggx2 = geometrySchlickGGX ( NdotV , roughness ) ; float ggx1 = geometrySchlickGGX ( NdotL , roughness ) ; return ggx1 * ggx2 ; }\n"
private const val DEF_FRESNELSCHLICK = "vec3 fresnelSchlick ( float cosTheta , vec3 F0 ) { return addv3 ( F0 , mulv3 ( subv3 ( ftov3 ( 1.0f ) , F0 ) , ftov3 ( pow ( 1.0f - cosTheta , 5.0f ) ) ) ) ; }\n"
private const val DEF_SHADINGPBR = "vec4 shadingPbr ( vec3 eye , vec3 worldPos , vec3 albedo , vec3 N , float metallic , float roughness , float ao ) { vec3 alb = powv3 ( albedo , ftov3 ( 2.2f ) ) ; vec3 V = normv3 ( subv3 ( eye , worldPos ) ) ; vec3 F0 = ftov3 ( 0.04f ) ; F0 = mixv3 ( F0 , alb , metallic ) ; vec3 Lo = v3zero ( ) ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { vec3 toLight = subv3 ( uLights [ i ] . vector , worldPos ) ; vec3 L = normv3 ( toLight ) ; vec3 H = normv3 ( addv3 ( V , L ) ) ; float distance = lenv3 ( toLight ) ; float lum = luminosity ( distance , uLights [ i ] ) ; vec3 radiance = mulv3 ( uLights [ i ] . color , ftov3 ( lum ) ) ; float NDF = distributionGGX ( N , H , roughness ) ; float G = geometrySmith ( N , V , L , roughness ) ; vec3 F = fresnelSchlick ( max ( dotv3 ( H , V ) , 0.0f ) , F0 ) ; vec3 nominator = mulv3 ( F , ftov3 ( NDF * G ) ) ; float denominator = 4.0f * max ( dotv3 ( N , V ) , 0.0f ) * max ( dotv3 ( N , L ) , 0.0f ) + 0.001f ; vec3 specular = divv3f ( nominator , denominator ) ; vec3 kD = subv3 ( ftov3 ( 1.0f ) , F ) ; kD = mulv3 ( kD , ftov3 ( 1.0f - metallic ) ) ; float NdotL = max ( dotv3 ( N , L ) , 0.0f ) ; Lo = addv3 ( Lo , mulv3 ( mulv3 ( addv3 ( divv3 ( mulv3 ( kD , alb ) , ftov3 ( PI ) ) , specular ) , radiance ) , ftov3 ( NdotL ) ) ) ; } vec3 ambient = mulv3 ( ftov3 ( 0.1f * ao ) , alb ) ; vec3 color = addv3 ( ambient , Lo ) ; color = divv3 ( color , addv3 ( color , ftov3 ( 1.0f ) ) ) ; color = powv3 ( color , ftov3 ( 1.0f / 2.2f ) ) ; return v3tov4 ( color , 1.0f ) ; }\n"
private const val DEF_CAMERALOOKAT = "RtCamera cameraLookAt ( vec3 eye , vec3 center , vec3 up , float vfoy , float aspect , float aperture , float focusDist ) { float lensRadius = aperture / 2.0f ; float halfHeight = tan ( vfoy / 2.0f ) ; float halfWidth = aspect * halfHeight ; vec3 w = normv3 ( subv3 ( eye , center ) ) ; vec3 u = normv3 ( crossv3 ( up , w ) ) ; vec3 v = crossv3 ( w , u ) ; vec3 hwu = mulv3f ( u , halfWidth * focusDist ) ; vec3 hhv = mulv3f ( v , halfHeight * focusDist ) ; vec3 wf = mulv3f ( w , focusDist ) ; vec3 lowerLeft = subv3 ( subv3 ( subv3 ( eye , hwu ) , hhv ) , wf ) ; vec3 horizontal = mulv3f ( u , halfWidth * focusDist * 2.0f ) ; vec3 vertical = mulv3f ( v , halfHeight * focusDist * 2.0f ) ; RtCamera result = { eye , lowerLeft , horizontal , vertical , w , u , v , lensRadius } ; return result ; }\n"
private const val DEF_RAYFROMCAMERA = "Ray rayFromCamera ( RtCamera camera , float u , float v ) { vec3 horShift = mulv3f ( camera . horizontal , u ) ; vec3 verShift = mulv3f ( camera . vertical , v ) ; vec3 origin ; vec3 direction ; if ( camera . lensRadius > 0.0f ) { vec3 rd = mulv3f ( randomInUnitDisk ( ) , camera . lensRadius ) ; vec3 offset = addv3 ( mulv3f ( camera . u , rd . x ) , mulv3f ( camera . v , rd . y ) ) ; origin = addv3 ( camera . origin , offset ) ; direction = normv3 ( subv3 ( subv3 ( addv3 ( camera . lowerLeft , addv3 ( horShift , verShift ) ) , camera . origin ) , offset ) ) ; } else { origin = camera . origin ; direction = normv3 ( subv3 ( addv3 ( camera . lowerLeft , addv3 ( horShift , verShift ) ) , camera . origin ) ) ; } Ray result = { origin , direction } ; return result ; }\n"
private const val DEF_BACKGROUND = "vec3 background ( Ray ray ) { float t = ( ray . direction . y + 1.0f ) * 0.5f ; vec3 gradient = lerpv3 ( v3one ( ) , v3 ( 0.5f , 0.7f , 1.0f ) , t ) ; return gradient ; }\n"
private const val DEF_RAYHITAABB = "bool rayHitAabb ( Ray ray , AABB aabb , float tMin , float tMax ) { for ( int i = 0 ; i < 3 ; i ++ ) { float invD = 1.0f / indexv3 ( ray . direction , i ) ; float t0 = ( indexv3 ( aabb . pointMin , i ) - indexv3 ( ray . origin , i ) ) * invD ; float t1 = ( indexv3 ( aabb . pointMax , i ) - indexv3 ( ray . origin , i ) ) * invD ; if ( invD < 0.0f ) { float temp = t0 ; t0 = t1 ; t1 = temp ; } float tmin = t0 > tMin ? t0 : tMin ; float tmax = t1 < tMax ? t1 : tMax ; if ( tmax <= tmin ) { return false ; } } return true ; }\n"
private const val DEF_RAYSPHEREHITRECORD = "HitRecord raySphereHitRecord ( Ray ray , float t , Sphere sphere ) { vec3 point = rayPoint ( ray , t ) ; vec3 N = normv3 ( divv3f ( subv3 ( point , sphere . center ) , sphere . radius ) ) ; HitRecord result = { t , point , N , sphere . materialType , sphere . materialIndex } ; return result ; }\n"
private const val DEF_RAYHITSPHERE = "HitRecord rayHitSphere ( Ray ray , float tMin , float tMax , Sphere sphere ) { vec3 oc = subv3 ( ray . origin , sphere . center ) ; float a = dotv3 ( ray . direction , ray . direction ) ; float b = 2 * dotv3 ( oc , ray . direction ) ; float c = dotv3 ( oc , oc ) - sphere . radius * sphere . radius ; float D = b * b - 4 * a * c ; if ( D > 0 ) { float t = ( - b - sqrt ( D ) ) / 2 * a ; if ( t < tMax && t > tMin ) { return raySphereHitRecord ( ray , t , sphere ) ; } t = ( - b + sqrt ( D ) ) / 2 * a ; if ( t < tMax && t > tMin ) { return raySphereHitRecord ( ray , t , sphere ) ; } } return NO_HIT ; }\n"
private const val DEF_RAYHITOBJECT = "HitRecord rayHitObject ( Ray ray , float tMin , float tMax , int type , int index ) { if ( type != HITABLE_SPHERE ) { flagError ( ) ; return NO_HIT ; } return rayHitSphere ( ray , tMin , tMax , uSpheres [ index ] ) ; }\n"
private const val DEF_RAYHITBVH = "HitRecord rayHitBvh ( Ray ray , float tMin , float tMax , int index ) { bvhTop = 0 ; float closest = tMax ; HitRecord result = NO_HIT ; int curr = index ; while ( curr >= 0 ) { while ( curr >= 0 && rayHitAabb ( ray , uBvhNodes [ curr ] . aabb , tMin , closest ) ) { if ( uBvhNodes [ curr ] . leftType == HITABLE_BVH ) { bvhStack [ bvhTop ] = curr ; bvhTop ++ ; curr = uBvhNodes [ curr ] . leftIndex ; } else { HitRecord hit = rayHitObject ( ray , tMin , closest , uBvhNodes [ curr ] . leftType , uBvhNodes [ curr ] . leftIndex ) ; if ( hit . t > 0 && hit . t < closest ) { result = hit ; closest = hit . t ; } break ; } } bvhTop -- ; if ( bvhTop < 0 ) { break ; } curr = bvhStack [ bvhTop ] ; curr = uBvhNodes [ curr ] . rightIndex ; } return result ; }\n"
private const val DEF_RAYHITWORLD = "HitRecord rayHitWorld ( Ray ray , float tMin , float tMax ) { return rayHitBvh ( ray , tMin , tMax , 0 ) ; }\n"
private const val DEF_MATERIALSCATTERLAMBERTIAN = "ScatterResult materialScatterLambertian ( HitRecord record , LambertianMaterial material ) { vec3 tangent = addv3 ( record . point , record . normal ) ; vec3 direction = addv3 ( tangent , randomInUnitSphere ( ) ) ; ScatterResult result = { material . albedo , { record . point , subv3 ( direction , record . point ) } } ; return result ; }\n"
private const val DEF_MATERIALSCATTERMETALIC = "ScatterResult materialScatterMetalic ( Ray ray , HitRecord record , MetallicMaterial material ) { vec3 reflected = reflectv3 ( ray . direction , record . normal ) ; if ( dotv3 ( reflected , record . normal ) > 0 ) { ScatterResult result = { material . albedo , { record . point , reflected } } ; return result ; } else { return NO_SCATTER ; } }\n"
private const val DEF_MATERIALSCATTERDIELECTRIC = "ScatterResult materialScatterDielectric ( Ray ray , HitRecord record , DielectricMaterial material ) { float niOverNt ; float cosine ; vec3 outwardNormal ; float rdotn = dotv3 ( ray . direction , record . normal ) ; float dirlen = lenv3 ( ray . direction ) ; if ( rdotn > 0 ) { outwardNormal = negv3 ( record . normal ) ; niOverNt = material . reflectiveIndex ; cosine = material . reflectiveIndex * rdotn / dirlen ; } else { outwardNormal = record . normal ; niOverNt = 1.0f / material . reflectiveIndex ; cosine = - rdotn / dirlen ; } float reflectProbe ; RefractResult refractResult = refractv3 ( ray . direction , outwardNormal , niOverNt ) ; if ( refractResult . isRefracted ) { reflectProbe = schlick ( cosine , material . reflectiveIndex ) ; } else { reflectProbe = 1.0f ; } vec3 scatteredDir ; if ( seededRndf ( ) < reflectProbe ) { scatteredDir = reflectv3 ( ray . direction , record . normal ) ; } else { scatteredDir = refractResult . refracted ; } ScatterResult scatterResult = { v3one ( ) , { record . point , scatteredDir } } ; return scatterResult ; }\n"
private const val DEF_MATERIALSCATTER = "ScatterResult materialScatter ( Ray ray , HitRecord record ) { switch ( record . materialType ) { case MATERIAL_LAMBERTIAN : return materialScatterLambertian ( record , uLambertianMaterials [ record . materialIndex ] ) ; case MATERIAL_METALIIC : return materialScatterMetalic ( ray , record , uMetallicMaterials [ record . materialIndex ] ) ; case MATERIAL_DIELECTRIC : return materialScatterDielectric ( ray , record , uDielectricMaterials [ record . materialIndex ] ) ; default : return NO_SCATTER ; } }\n"
private const val DEF_SAMPLECOLOR = "vec3 sampleColor ( int rayBounces , RtCamera camera , float u , float v ) { Ray ray = rayFromCamera ( camera , u , v ) ; vec3 fraction = ftov3 ( 1.0f ) ; for ( int i = 0 ; i < rayBounces ; i ++ ) { HitRecord record = rayHitWorld ( ray , BOUNCE_ERR , FLT_MAX ) ; if ( record . t < 0 ) { break ; } else { ScatterResult scatterResult = materialScatter ( ray , record ) ; if ( scatterResult . attenuation . x < 0 ) { return v3zero ( ) ; } fraction = mulv3 ( fraction , scatterResult . attenuation ) ; ray = scatterResult . scattered ; } } return mulv3 ( background ( ray ) , fraction ) ; }\n"
private const val DEF_FRAGMENTCOLORRT = "vec4 fragmentColorRt ( int width , int height , float random , int sampleCnt , int rayBounces , vec3 eye , vec3 center , vec3 up , float fovy , float aspect , float aperture , float focusDist , vec2 texCoord ) { seedRandom ( v2tov3 ( texCoord , random ) ) ; float DU = 1.0f / itof ( width ) ; float DV = 1.0f / itof ( height ) ; RtCamera camera = cameraLookAt ( eye , center , up , fovy , aspect , aperture , focusDist ) ; vec3 result = v3zero ( ) ; for ( int i = 0 ; i < sampleCnt ; i ++ ) { float du = DU * seededRndf ( ) ; float dv = DV * seededRndf ( ) ; float sampleU = texCoord . x + du ; float sampleV = texCoord . y + dv ; result = addv3 ( result , sampleColor ( rayBounces , camera , sampleU , sampleV ) ) ; } return v3tov4 ( result , 1.0f ) ; }\n"
private const val DEF_GAMMASQRT = "vec4 gammaSqrt ( vec4 result ) { return v4 ( sqrt ( result . x ) , sqrt ( result . y ) , sqrt ( result . z ) , 1.0f ) ; }\n"

const val TYPES_DEF = DEF_RAY+DEF_AABB+DEF_RTCAMERA+DEF_LIGHT+DEF_PHONGMATERIAL+DEF_BVHNODE+DEF_SPHERE+DEF_LAMBERTIANMATERIAL+DEF_METALLICMATERIAL+DEF_DIELECTRICMATERIAL+DEF_HITRECORD+DEF_SCATTERRESULT+DEF_REFRACTRESULT

const val OPS_DEF = DEF_FLAGERROR+DEF_FTOV2+DEF_V2ZERO+DEF_GETXV2+DEF_GETYV2+DEF_GETUV2+DEF_GETVV2+DEF_INDEXV3+DEF_V2TOV3+DEF_FTOV3+DEF_V3ZERO+DEF_V3ONE+DEF_V3FRONT+DEF_V3BACK+DEF_V3LEFT+DEF_V3RIGHT+DEF_V3UP+DEF_V3DOWN+DEF_V3WHITE+DEF_V3BLACK+DEF_V3LTGREY+DEF_V3GREY+DEF_V3DKGREY+DEF_V3RED+DEF_V3GREEN+DEF_V3BLUE+DEF_V3YELLOW+DEF_V3MAGENTA+DEF_V3CYAN+DEF_V3ORANGE+DEF_V3ROSE+DEF_V3VIOLET+DEF_V3AZURE+DEF_V3AQUAMARINE+DEF_V3CHARTREUSE+DEF_V3TOV4+DEF_FTOV4+DEF_V4ZERO+DEF_GETXV4+DEF_GETYV4+DEF_GETZV4+DEF_GETWV4+DEF_GETRV4+DEF_GETGV4+DEF_GETBV4+DEF_GETAV4+DEF_SETXV4+DEF_SETYV4+DEF_SETZV4+DEF_SETWV4+DEF_SETRV4+DEF_SETGV4+DEF_SETBV4+DEF_SETAV4+DEF_RAYBACK+DEF_EQV2+DEF_EQV3+DEF_EQV4+DEF_SQRTV+DEF_SINV+DEF_COSV+DEF_TANV+DEF_POWV+DEF_MINV+DEF_MAXV+DEF_NEGV3+DEF_ADDF+DEF_SUBF+DEF_MULF+DEF_DIVF+DEF_POWV3+DEF_MIXV3+DEF_ADDV4+DEF_SUBV4+DEF_MULV4+DEF_MULV4F+DEF_DIVV4+DEF_DIVV4F+DEF_LENV3+DEF_LENSQV3+DEF_NORMV3+DEF_LERPV3+DEF_RAYPOINT+DEF_SCHLICK+DEF_REFLECTV3+DEF_REFRACTV3+DEF_RANDOMINUNITSPHERE+DEF_RANDOMINUNITDISK+DEF_ERRORHANDLER+DEF_TILE+DEF_LUMINOSITY+DEF_DIFFUSECONTRIB+DEF_HALFVECTOR+DEF_SPECULARCONTRIB+DEF_LIGHTCONTRIB+DEF_POINTLIGHTCONTRIB+DEF_DIRLIGHTCONTRIB+DEF_SHADINGFLAT+DEF_SHADINGPHONG+DEF_DISTRIBUTIONGGX+DEF_GEOMETRYSCHLICKGGX+DEF_GEOMETRYSMITH+DEF_FRESNELSCHLICK+DEF_SHADINGPBR+DEF_CAMERALOOKAT+DEF_RAYFROMCAMERA+DEF_BACKGROUND+DEF_RAYHITAABB+DEF_RAYSPHEREHITRECORD+DEF_RAYHITSPHERE+DEF_RAYHITOBJECT+DEF_RAYHITBVH+DEF_RAYHITWORLD+DEF_MATERIALSCATTERLAMBERTIAN+DEF_MATERIALSCATTERMETALIC+DEF_MATERIALSCATTERDIELECTRIC+DEF_MATERIALSCATTER+DEF_SAMPLECOLOR+DEF_FRAGMENTCOLORRT+DEF_GAMMASQRT

const val CONST_DEF = DEF_PI+DEF_BOUNCE_ERR+DEF_NO_HIT+DEF_NO_SCATTER+DEF_NO_REFRACT

fun flagError() = object : Expression<Int>() {
    override fun expr() = "flagError()"
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

fun iv2(x: Expression<Int>, y: Expression<Int>) = object : Expression<vec2i>() {
    override fun expr() = "iv2(${x.expr()}, ${y.expr()})"
    override fun roots() = listOf(x, y)
}

fun getxv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getxv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getyv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getyv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getuv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getuv2(${v.expr()})"
    override fun roots() = listOf(v)
}

fun getvv2(v: Expression<vec2>) = object : Expression<Float>() {
    override fun expr() = "getvv2(${v.expr()})"
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

fun v4zero() = object : Expression<vec4>() {
    override fun expr() = "v4zero()"
    override fun roots() = listOf<Expression<*>>()
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

fun rayBack() = object : Expression<ray>() {
    override fun expr() = "rayBack()"
    override fun roots() = listOf<Expression<*>>()
}

fun eqv2(left: Expression<vec2>, right: Expression<vec2>) = object : Expression<Boolean>() {
    override fun expr() = "eqv2(${left.expr()}, ${right.expr()})"
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

fun sqrtv(value: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sqrtv(${value.expr()})"
    override fun roots() = listOf(value)
}

fun sinv(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "sinv(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun cosv(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "cosv(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun tanv(rad: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "tanv(${rad.expr()})"
    override fun roots() = listOf(rad)
}

fun powv(base: Expression<Float>, power: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "powv(${base.expr()}, ${power.expr()})"
    override fun roots() = listOf(base, power)
}

fun minv(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "minv(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun maxv(left: Expression<Float>, right: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "maxv(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun negv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "negv3(${v.expr()})"
    override fun roots() = listOf(v)
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

fun lenv3(v: Expression<vec3>) = object : Expression<Float>() {
    override fun expr() = "lenv3(${v.expr()})"
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

fun rayPoint(ray: Expression<ray>, t: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "rayPoint(${ray.expr()}, ${t.expr()})"
    override fun roots() = listOf(ray, t)
}

fun schlick(cosine: Expression<Float>, ri: Expression<Float>) = object : Expression<Float>() {
    override fun expr() = "schlick(${cosine.expr()}, ${ri.expr()})"
    override fun roots() = listOf(cosine, ri)
}

fun reflectv3(v: Expression<vec3>, n: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "reflectv3(${v.expr()}, ${n.expr()})"
    override fun roots() = listOf(v, n)
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

fun errorHandler(color: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "errorHandler(${color.expr()})"
    override fun roots() = listOf(color)
}

fun tile(texCoord: Expression<vec2>, uv: Expression<vec2i>, cnt: Expression<vec2i>) = object : Expression<vec2>() {
    override fun expr() = "tile(${texCoord.expr()}, ${uv.expr()}, ${cnt.expr()})"
    override fun roots() = listOf(texCoord, uv, cnt)
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

fun fragmentColorRt(width: Expression<Int>, height: Expression<Int>, random: Expression<Float>, sampleCnt: Expression<Int>, rayBounces: Expression<Int>, eye: Expression<vec3>, center: Expression<vec3>, up: Expression<vec3>, fovy: Expression<Float>, aspect: Expression<Float>, aperture: Expression<Float>, focusDist: Expression<Float>, texCoord: Expression<vec2>) = object : Expression<vec4>() {
    override fun expr() = "fragmentColorRt(${width.expr()}, ${height.expr()}, ${random.expr()}, ${sampleCnt.expr()}, ${rayBounces.expr()}, ${eye.expr()}, ${center.expr()}, ${up.expr()}, ${fovy.expr()}, ${aspect.expr()}, ${aperture.expr()}, ${focusDist.expr()}, ${texCoord.expr()})"
    override fun roots() = listOf(width, height, random, sampleCnt, rayBounces, eye, center, up, fovy, aspect, aperture, focusDist, texCoord)
}

fun gammaSqrt(result: Expression<vec4>) = object : Expression<vec4>() {
    override fun expr() = "gammaSqrt(${result.expr()})"
    override fun roots() = listOf(result)
}


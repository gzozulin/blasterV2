package com.gzozulin.minigl.api

import com.gzozulin.minigl.scene.Light
import com.gzozulin.minigl.scene.HitRecord
import com.gzozulin.minigl.scene.Sphere
import com.gzozulin.minigl.scene.PhongMaterial

private const val DEF_FTOV2 = "vec2 ftov2 ( float v ) { return v2 ( v , v ) ; }\n\n"
private const val DEF_V2ZERO = "vec2 v2zero ( ) { return ftov2 ( 0.0f ) ; }\n\n"
private const val DEF_FTOV3 = "vec3 ftov3 ( float v ) { return v3 ( v , v , v ) ; }\n\n"
private const val DEF_V3ZERO = "vec3 v3zero ( ) { return ftov3 ( 0.0f ) ; }\n\n"
private const val DEF_V3ONE = "vec3 v3one ( ) { return ftov3 ( 1.0f ) ; }\n\n"
private const val DEF_V3FRONT = "vec3 v3front ( ) { return v3 ( 0 , 0 , 1 ) ; }\n\n"
private const val DEF_V3BACK = "vec3 v3back ( ) { return v3 ( 0 , 0 , - 1 ) ; }\n\n"
private const val DEF_V3LEFT = "vec3 v3left ( ) { return v3 ( - 1 , 0 , 0 ) ; }\n\n"
private const val DEF_V3RIGHT = "vec3 v3right ( ) { return v3 ( 1 , 0 , 0 ) ; }\n\n"
private const val DEF_V3UP = "vec3 v3up ( ) { return v3 ( 0 , 1 , 0 ) ; }\n\n"
private const val DEF_V3DOWN = "vec3 v3down ( ) { return v3 ( 0 , - 1 , 0 ) ; }\n\n"
private const val DEF_V3WHITE = "vec3 v3white ( ) { return v3 ( 1.0f , 1.0f , 1.0f ) ; }\n\n"
private const val DEF_V3BLACK = "vec3 v3black ( ) { return v3 ( 0.0f , 0.0f , 0.0f ) ; }\n\n"
private const val DEF_V3LTGREY = "vec3 v3ltGrey ( ) { return ftov3 ( 0.3f ) ; }\n\n"
private const val DEF_V3GREY = "vec3 v3grey ( ) { return ftov3 ( 0.5f ) ; }\n\n"
private const val DEF_V3DKGREY = "vec3 v3dkGrey ( ) { return ftov3 ( 0.7f ) ; }\n\n"
private const val DEF_V3RED = "vec3 v3red ( ) { return v3 ( 1.0f , 0.0f , 0.0f ) ; }\n\n"
private const val DEF_V3GREEN = "vec3 v3green ( ) { return v3 ( 0.0f , 1.0f , 0.0f ) ; }\n\n"
private const val DEF_V3BLUE = "vec3 v3blue ( ) { return v3 ( 0.0f , 0.0f , 1.0f ) ; }\n\n"
private const val DEF_V3YELLOW = "vec3 v3yellow ( ) { return v3 ( 1.0f , 1.0f , 0.0f ) ; }\n\n"
private const val DEF_V3MAGENTA = "vec3 v3magenta ( ) { return v3 ( 1.0f , 0.0f , 1.0f ) ; }\n\n"
private const val DEF_V3CYAN = "vec3 v3cyan ( ) { return v3 ( 0.0f , 1.0f , 1.0f ) ; }\n\n"
private const val DEF_V3ORANGE = "vec3 v3orange ( ) { return v3 ( 1.0f , 0.5f , 0.0f ) ; }\n\n"
private const val DEF_V3ROSE = "vec3 v3rose ( ) { return v3 ( 1.0f , 0.0f , 0.5f ) ; }\n\n"
private const val DEF_V3VIOLET = "vec3 v3violet ( ) { return v3 ( 0.5f , 0.0f , 1.0f ) ; }\n\n"
private const val DEF_V3AZURE = "vec3 v3azure ( ) { return v3 ( 0.0f , 0.5f , 1.0f ) ; }\n\n"
private const val DEF_V3AQUAMARINE = "vec3 v3aquamarine ( ) { return v3 ( 0.0f , 1.0f , 0.5f ) ; }\n\n"
private const val DEF_V3CHARTREUSE = "vec3 v3chartreuse ( ) { return v3 ( 0.5f , 1.0f , 0.0f ) ; }\n\n"
private const val DEF_V3TOV4 = "vec4 v3tov4 ( vec3 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n\n"
private const val DEF_FTOV4 = "vec4 ftov4 ( float v ) { return v4 ( v , v , v , v ) ; }\n\n"
private const val DEF_V4ZERO = "vec4 v4zero ( ) { return ftov4 ( 0.0f ) ; }\n\n"
private const val DEF_RAYFRONT = "ray rayFront ( ) { ray result = { v3zero ( ) , v3front ( ) } ; return result ; }\n\n"
private const val DEF_GETXV4 = "float getxv4 ( vec4 v ) { return v . x ; }\n\n"
private const val DEF_GETYV4 = "float getyv4 ( vec4 v ) { return v . y ; }\n\n"
private const val DEF_GETZV4 = "float getzv4 ( vec4 v ) { return v . z ; }\n\n"
private const val DEF_GETWV4 = "float getwv4 ( vec4 v ) { return v . w ; }\n\n"
private const val DEF_GETRV4 = "float getrv4 ( vec4 v ) { return v . x ; }\n\n"
private const val DEF_GETGV4 = "float getgv4 ( vec4 v ) { return v . y ; }\n\n"
private const val DEF_GETBV4 = "float getbv4 ( vec4 v ) { return v . z ; }\n\n"
private const val DEF_GETAV4 = "float getav4 ( vec4 v ) { return v . w ; }\n\n"
private const val DEF_SETXV4 = "vec4 setxv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n\n"
private const val DEF_SETYV4 = "vec4 setyv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n\n"
private const val DEF_SETZV4 = "vec4 setzv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n\n"
private const val DEF_SETWV4 = "vec4 setwv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n\n"
private const val DEF_SETRV4 = "vec4 setrv4 ( vec4 v , float f ) { return v4 ( f , v . y , v . z , v . w ) ; }\n\n"
private const val DEF_SETGV4 = "vec4 setgv4 ( vec4 v , float f ) { return v4 ( v . x , f , v . z , v . w ) ; }\n\n"
private const val DEF_SETBV4 = "vec4 setbv4 ( vec4 v , float f ) { return v4 ( v . x , v . y , f , v . w ) ; }\n\n"
private const val DEF_SETAV4 = "vec4 setav4 ( vec4 v , float f ) { return v4 ( v . x , v . y , v . z , f ) ; }\n\n"
private const val DEF_EQV2 = "bool eqv2 ( vec2 left , vec2 right ) { return left . x == right . x && left . y == right . y ; }\n\n"
private const val DEF_EQV3 = "bool eqv3 ( vec3 left , vec3 right ) { return left . x == right . x && left . y == right . y && left . z == right . z ; }\n\n"
private const val DEF_EQV4 = "bool eqv4 ( vec4 left , vec4 right ) { return left . x == right . x && left . y == right . y && left . z == right . z && left . w == right . w ; }\n\n"
private const val DEF_NEGV3 = "vec3 negv3 ( vec3 v ) { return v3 ( - v . x , - v . y , - v . z ) ; }\n\n"
private const val DEF_DOTV3 = "float dotv3 ( vec3 left , vec3 right ) { return left . x * right . x + left . y * right . y + left . z * right . z ; }\n\n"
private const val DEF_CROSSV3 = "vec3 crossv3 ( vec3 left , vec3 right ) { return v3 ( left . y * right . z - left . z * right . y , left . z * right . x - left . x * right . z , left . x * right . y - left . y * right . x ) ; }\n\n"
private const val DEF_ADDV3 = "vec3 addv3 ( vec3 left , vec3 right ) { return v3 ( left . x + right . x , left . y + right . y , left . z + right . z ) ; }\n\n"
private const val DEF_SUBV3 = "vec3 subv3 ( vec3 left , vec3 right ) { return v3 ( left . x - right . x , left . y - right . y , left . z - right . z ) ; }\n\n"
private const val DEF_MULV3 = "vec3 mulv3 ( vec3 left , vec3 right ) { return v3 ( left . x * right . x , left . y * right . y , left . z * right . z ) ; }\n\n"
private const val DEF_MULV3F = "vec3 mulv3f ( vec3 left , float right ) { return v3 ( left . x * right , left . y * right , left . z * right ) ; }\n\n"
private const val DEF_POWV3 = "vec3 powv3 ( vec3 left , vec3 right ) { return v3 ( pow ( left . x , right . x ) , pow ( left . y , right . y ) , pow ( left . z , right . z ) ) ; }\n\n"
private const val DEF_DIVV3F = "vec3 divv3f ( vec3 left , float right ) { return v3 ( left . x / right , left . y / right , left . z / right ) ; }\n\n"
private const val DEF_DIVV3 = "vec3 divv3 ( vec3 left , vec3 right ) { return v3 ( left . x / right . x , left . y / right . y , left . z / right . z ) ; }\n\n"
private const val DEF_MIXV3 = "vec3 mixv3 ( vec3 left , vec3 right , float proportion ) { return addv3 ( mulv3 ( left , ftov3 ( 1.0f - proportion ) ) , mulv3 ( right , ftov3 ( proportion ) ) ) ; }\n\n"
private const val DEF_ADDV4 = "vec4 addv4 ( vec4 left , vec4 right ) { return v4 ( left . x + right . x , left . y + right . y , left . z + right . z , left . w + right . w ) ; }\n\n"
private const val DEF_SUBV4 = "vec4 subv4 ( vec4 left , vec4 right ) { return v4 ( left . x - right . x , left . y - right . y , left . z - right . z , left . w - right . w ) ; }\n\n"
private const val DEF_MULV4 = "vec4 mulv4 ( vec4 left , vec4 right ) { return v4 ( left . x * right . x , left . y * right . y , left . z * right . z , left . w * right . w ) ; }\n\n"
private const val DEF_MULV4F = "vec4 mulv4f ( vec4 left , float right ) { return v4 ( left . x * right , left . y * right , left . z * right , left . w * right ) ; }\n\n"
private const val DEF_DIVV4 = "vec4 divv4 ( vec4 left , vec4 right ) { return v4 ( left . x / right . x , left . y / right . y , left . z / right . z , left . w / right . w ) ; }\n\n"
private const val DEF_DIVV4F = "vec4 divv4f ( vec4 left , float right ) { return v4 ( left . x / right , left . y / right , left . z / right , left . z / right ) ; }\n\n"
private const val DEF_LENV3 = "float lenv3 ( vec3 v ) { return sqrt ( v . x * v . x + v . y * v . y + v . z * v . z ) ; }\n\n"
private const val DEF_NORMV3 = "vec3 normv3 ( vec3 v ) { return divv3f ( v , lenv3 ( v ) ) ; }\n\n"
private const val DEF_LERPV3 = "vec3 lerpv3 ( vec3 from , vec3 to , float t ) { return addv3 ( mulv3f ( from , 1.0f - t ) , mulv3f ( to , t ) ) ; }\n\n"
private const val DEF_POINTONRAY = "vec3 pointOnRay ( ray ray , float t ) { return addv3 ( ray . origin , mulv3f ( ray . direction , t ) ) ; }\n\n"
private const val DEF_TILE = "vec2 tile ( vec2 texCoord , ivec2 uv , ivec2 cnt ) { float tileSideX = 1.0f / itof ( cnt . x ) ; float tileStartX = itof ( uv . x ) * tileSideX ; float tileSideY = 1.0f / itof ( cnt . y ) ; float tileStartY = itof ( uv . y ) * tileSideY ; return v2 ( tileStartX + texCoord . x * tileSideX , tileStartY + texCoord . y * tileSideY ) ; }\n\n"
private const val DEF_LUMINOSITY = "float luminosity ( float distance , Light light ) { return 1.0f / ( light . attenConstant + light . attenLinear * distance + light . attenQuadratic * distance * distance ) ; }\n\n"
private const val DEF_DIFFUSECONTRIB = "vec3 diffuseContrib ( vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { float diffuseTerm = dotv3 ( fragNormal , lightDir ) ; return diffuseTerm > 0.0f ? mulv3f ( material . diffuse , diffuseTerm ) : v3zero ( ) ; }\n\n"
private const val DEF_HALFVECTOR = "vec3 halfVector ( vec3 left , vec3 right ) { return normv3 ( addv3 ( left , right ) ) ; }\n\n"
private const val DEF_SPECULARCONTRIB = "vec3 specularContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , PhongMaterial material ) { vec3 hv = halfVector ( viewDir , lightDir ) ; float specularTerm = dotv3 ( hv , fragNormal ) ; return specularTerm > 0.0f ? mulv3f ( material . specular , pow ( specularTerm , material . shine ) ) : v3zero ( ) ; }\n\n"
private const val DEF_LIGHTCONTRIB = "vec3 lightContrib ( vec3 viewDir , vec3 lightDir , vec3 fragNormal , float attenuation , Light light , PhongMaterial material ) { vec3 lighting = v3zero ( ) ; lighting = addv3 ( lighting , diffuseContrib ( lightDir , fragNormal , material ) ) ; lighting = addv3 ( lighting , specularContrib ( viewDir , lightDir , fragNormal , material ) ) ; return mulv3 ( mulv3f ( light . color , attenuation ) , lighting ) ; }\n\n"
private const val DEF_POINTLIGHTCONTRIB = "vec3 pointLightContrib ( vec3 viewDir , vec3 fragPosition , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 direction = subv3 ( light . vector , fragPosition ) ; vec3 lightDir = normv3 ( direction ) ; if ( dotv3 ( lightDir , fragNormal ) < 0.0f ) { return v3zero ( ) ; } float distance = lenv3 ( direction ) ; float lum = luminosity ( distance , light ) ; return lightContrib ( viewDir , lightDir , fragNormal , lum , light , material ) ; }\n\n"
private const val DEF_DIRLIGHTCONTRIB = "vec3 dirLightContrib ( vec3 viewDir , vec3 fragNormal , Light light , PhongMaterial material ) { vec3 lightDir = negv3 ( normv3 ( light . vector ) ) ; return lightContrib ( viewDir , lightDir , fragNormal , 1.0f , light , material ) ; }\n\n"
private const val DEF_SHADINGFLAT = "vec4 shadingFlat ( vec4 color ) { return color ; }\n\n"
private const val DEF_SHADINGPHONG = "vec4 shadingPhong ( vec3 fragPosition , vec3 eye , vec3 fragNormal , vec3 fragAlbedo , PhongMaterial material ) { vec3 viewDir = normv3 ( subv3 ( eye , fragPosition ) ) ; vec3 color = material . ambient ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { color = addv3 ( color , pointLightContrib ( viewDir , fragPosition , fragNormal , uLights [ i ] , material ) ) ; } for ( int i = uLightsPointCnt ; i < uLightsPointCnt + uLightsDirCnt ; ++ i ) { color = addv3 ( color , dirLightContrib ( viewDir , fragNormal , uLights [ i ] , material ) ) ; } color = mulv3 ( color , fragAlbedo ) ; return v3tov4 ( color , material . transparency ) ; }\n\n"
private const val DEF_DISTRIBUTIONGGX = "float distributionGGX ( vec3 N , vec3 H , float a ) { float a2 = a * a ; float NdotH = max ( dotv3 ( N , H ) , 0.0f ) ; float NdotH2 = NdotH * NdotH ; float nom = a2 ; float denom = ( NdotH2 * ( a2 - 1.0f ) + 1.0f ) ; denom = PI * denom * denom ; return nom / denom ; }\n\n"
private const val DEF_GEOMETRYSCHLICKGGX = "float geometrySchlickGGX ( float NdotV , float roughness ) { float r = ( roughness + 1.0f ) ; float k = ( r * r ) / 8.0f ; float nom = NdotV ; float denom = NdotV * ( 1.0f - k ) + k ; return nom / denom ; }\n\n"
private const val DEF_GEOMETRYSMITH = "float geometrySmith ( vec3 N , vec3 V , vec3 L , float roughness ) { float NdotV = max ( dotv3 ( N , V ) , 0.0f ) ; float NdotL = max ( dotv3 ( N , L ) , 0.0f ) ; float ggx2 = geometrySchlickGGX ( NdotV , roughness ) ; float ggx1 = geometrySchlickGGX ( NdotL , roughness ) ; return ggx1 * ggx2 ; }\n\n"
private const val DEF_FRESNELSCHLICK = "vec3 fresnelSchlick ( float cosTheta , vec3 F0 ) { return addv3 ( F0 , mulv3 ( subv3 ( ftov3 ( 1.0f ) , F0 ) , ftov3 ( pow ( 1.0f - cosTheta , 5.0f ) ) ) ) ; }\n\n"
private const val DEF_SHADINGPBR = "vec4 shadingPbr ( vec3 eye , vec3 worldPos , vec3 albedo , vec3 N , float metallic , float roughness , float ao ) { vec3 alb = powv3 ( albedo , ftov3 ( 2.2f ) ) ; vec3 V = normv3 ( subv3 ( eye , worldPos ) ) ; vec3 F0 = ftov3 ( 0.04f ) ; F0 = mixv3 ( F0 , alb , metallic ) ; vec3 Lo = v3zero ( ) ; for ( int i = 0 ; i < uLightsPointCnt ; ++ i ) { vec3 toLight = subv3 ( uLights [ i ] . vector , worldPos ) ; vec3 L = normv3 ( toLight ) ; vec3 H = normv3 ( addv3 ( V , L ) ) ; float distance = lenv3 ( toLight ) ; float lum = luminosity ( distance , uLights [ i ] ) ; vec3 radiance = mulv3 ( uLights [ i ] . color , ftov3 ( lum ) ) ; float NDF = distributionGGX ( N , H , roughness ) ; float G = geometrySmith ( N , V , L , roughness ) ; vec3 F = fresnelSchlick ( max ( dotv3 ( H , V ) , 0.0f ) , F0 ) ; vec3 nominator = mulv3 ( F , ftov3 ( NDF * G ) ) ; float denominator = 4.0f * max ( dotv3 ( N , V ) , 0.0f ) * max ( dotv3 ( N , L ) , 0.0f ) + 0.001f ; vec3 specular = divv3f ( nominator , denominator ) ; vec3 kD = subv3 ( ftov3 ( 1.0f ) , F ) ; kD = mulv3 ( kD , ftov3 ( 1.0f - metallic ) ) ; float NdotL = max ( dotv3 ( N , L ) , 0.0f ) ; Lo = addv3 ( Lo , mulv3 ( mulv3 ( addv3 ( divv3 ( mulv3 ( kD , alb ) , ftov3 ( PI ) ) , specular ) , radiance ) , ftov3 ( NdotL ) ) ) ; } vec3 ambient = mulv3 ( ftov3 ( 0.1f * ao ) , alb ) ; vec3 color = addv3 ( ambient , Lo ) ; color = divv3 ( color , addv3 ( color , ftov3 ( 1.0f ) ) ) ; color = powv3 ( color , ftov3 ( 1.0f / 2.2f ) ) ; return v3tov4 ( color , 1.0f ) ; }\n\n"
private const val DEF_BACKGROUND = "vec4 background ( ray ray ) { float t = ( ray . direction . y + 1.0f ) * 0.5f ; vec3 gradient = lerpv3 ( v3one ( ) , v3 ( 0.5f , 0.7f , 1.0f ) , t ) ; return v3tov4 ( gradient , 1.0f ) ; }\n\n"
private const val DEF_CREATERAYFROMTEXCOORD = "ray createRayFromTexCoord ( vec2 texCoord ) { vec3 lowerLeft = { - 1 , - 1 , - 1 } ; vec3 origin = { 0 , 0 , 0 } ; vec3 horizontal = { 2 , 0 , 0 } ; vec3 vertical = { 0 , 2 , 0 } ; vec3 direction = normv3 ( addv3 ( lowerLeft , addv3 ( mulv3f ( horizontal , texCoord . x ) , mulv3f ( vertical , texCoord . y ) ) ) ) ; ray result = { origin , direction } ; return result ; }\n\n"
private const val DEF_CREATERAYSPHEREHITRECORD = "HitRecord createRaySphereHitRecord ( ray ray , float t , Sphere sphere ) { vec3 point = pointOnRay ( ray , t ) ; vec3 N = normv3 ( divv3f ( subv3 ( point , sphere . center ) , sphere . radius ) ) ; HitRecord result = { t , point , N } ; return result ; }\n\n"
private const val DEF_HITRAYSPHERE = "HitRecord hitRaySphere ( ray ray , float tMin , float tMax , Sphere sphere ) { vec3 oc = subv3 ( ray . origin , sphere . center ) ; float a = dotv3 ( ray . direction , ray . direction ) ; float b = 2 * dotv3 ( oc , ray . direction ) ; float c = dotv3 ( oc , oc ) - sphere . radius * sphere . radius ; float D = b * b - 4 * a * c ; if ( D > 0 ) { float temp = ( - b - sqrt ( D ) ) / 2 * a ; if ( temp < tMax && temp > tMin ) { return createRaySphereHitRecord ( ray , temp , sphere ) ; } temp = ( - b + sqrt ( D ) ) / 2 * a ; if ( temp < tMax && temp > tMin ) { return createRaySphereHitRecord ( ray , temp , sphere ) ; } } return NO_HIT ; }\n\n"
private const val DEF_HITRAYHITABLES = "HitRecord hitRayHitables ( ray ray , float tMin , float tMax ) { HitRecord result = NO_HIT ; float closest = tMax ; for ( int i = 0 ; i < uHitablesCnt ; i ++ ) { Hitable hitable = uHitables [ i ] ; HitRecord hitRecord ; switch ( hitable . type ) { case HITABLE_SPHERE : hitRecord = hitRaySphere ( ray , tMin , closest , uSpheres [ hitable . index ] ) ; break ; default : hitRecord = NO_HIT ; } if ( hitRecord . t > 0 ) { closest = hitRecord . t ; result = hitRecord ; } } return result ; }\n\n"
private const val DEF_SHADINGRT = "vec4 shadingRt ( vec2 texCoord ) { ray ray = createRayFromTexCoord ( texCoord ) ; HitRecord record = hitRayHitables ( ray , 0 , 1000000 ) ; if ( record . t > 0 ) { return v3tov4 ( mulv3f ( addv3 ( record . normal , v3one ( ) ) , 0.5f ) , 1.0f ) ; } else { return background ( ray ) ; } }\n\n"

const val PUBLIC_DEFINITIONS = DEF_FTOV2+DEF_V2ZERO+DEF_FTOV3+DEF_V3ZERO+DEF_V3ONE+DEF_V3FRONT+DEF_V3BACK+DEF_V3LEFT+DEF_V3RIGHT+DEF_V3UP+DEF_V3DOWN+DEF_V3WHITE+DEF_V3BLACK+DEF_V3LTGREY+DEF_V3GREY+DEF_V3DKGREY+DEF_V3RED+DEF_V3GREEN+DEF_V3BLUE+DEF_V3YELLOW+DEF_V3MAGENTA+DEF_V3CYAN+DEF_V3ORANGE+DEF_V3ROSE+DEF_V3VIOLET+DEF_V3AZURE+DEF_V3AQUAMARINE+DEF_V3CHARTREUSE+DEF_V3TOV4+DEF_FTOV4+DEF_V4ZERO+DEF_RAYFRONT+DEF_GETXV4+DEF_GETYV4+DEF_GETZV4+DEF_GETWV4+DEF_GETRV4+DEF_GETGV4+DEF_GETBV4+DEF_GETAV4+DEF_SETXV4+DEF_SETYV4+DEF_SETZV4+DEF_SETWV4+DEF_SETRV4+DEF_SETGV4+DEF_SETBV4+DEF_SETAV4+DEF_EQV2+DEF_EQV3+DEF_EQV4+DEF_NEGV3+DEF_DOTV3+DEF_CROSSV3+DEF_ADDV3+DEF_SUBV3+DEF_MULV3+DEF_MULV3F+DEF_POWV3+DEF_DIVV3F+DEF_DIVV3+DEF_MIXV3+DEF_ADDV4+DEF_SUBV4+DEF_MULV4+DEF_MULV4F+DEF_DIVV4+DEF_DIVV4F+DEF_LENV3+DEF_NORMV3+DEF_LERPV3+DEF_POINTONRAY+DEF_TILE+DEF_LUMINOSITY+DEF_DIFFUSECONTRIB+DEF_HALFVECTOR+DEF_SPECULARCONTRIB+DEF_LIGHTCONTRIB+DEF_POINTLIGHTCONTRIB+DEF_DIRLIGHTCONTRIB+DEF_SHADINGFLAT+DEF_SHADINGPHONG+DEF_DISTRIBUTIONGGX+DEF_GEOMETRYSCHLICKGGX+DEF_GEOMETRYSMITH+DEF_FRESNELSCHLICK+DEF_SHADINGPBR+DEF_BACKGROUND+DEF_CREATERAYFROMTEXCOORD+DEF_CREATERAYSPHEREHITRECORD+DEF_HITRAYSPHERE+DEF_HITRAYHITABLES+DEF_SHADINGRT

fun itof(i: Expression<Int>) = object : Expression<Float>() {
    override fun expr() = "itof(${i.expr()})"
    override fun roots() = listOf(i)
}

fun ftoi(f: Expression<Float>) = object : Expression<Int>() {
    override fun expr() = "ftoi(${f.expr()})"
    override fun roots() = listOf(f)
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

fun v3(x: Expression<Float>, y: Expression<Float>, z: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "v3(${x.expr()}, ${y.expr()}, ${z.expr()})"
    override fun roots() = listOf(x, y, z)
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

fun m3ident() = object : Expression<mat3>() {
    override fun expr() = "m3ident()"
    override fun roots() = listOf<Expression<*>>()
}

fun rayFront() = object : Expression<ray>() {
    override fun expr() = "rayFront()"
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

fun mulv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "mulv3(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun mulv3f(left: Expression<vec3>, right: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "mulv3f(${left.expr()}, ${right.expr()})"
    override fun roots() = listOf(left, right)
}

fun powv3(left: Expression<vec3>, right: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "powv3(${left.expr()}, ${right.expr()})"
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

fun normv3(v: Expression<vec3>) = object : Expression<vec3>() {
    override fun expr() = "normv3(${v.expr()})"
    override fun roots() = listOf(v)
}

fun lerpv3(from: Expression<vec3>, to: Expression<vec3>, t: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "lerpv3(${from.expr()}, ${to.expr()}, ${t.expr()})"
    override fun roots() = listOf(from, to, t)
}

fun pointOnRay(ray: Expression<ray>, t: Expression<Float>) = object : Expression<vec3>() {
    override fun expr() = "pointOnRay(${ray.expr()}, ${t.expr()})"
    override fun roots() = listOf(ray, t)
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

fun background(ray: Expression<ray>) = object : Expression<vec4>() {
    override fun expr() = "background(${ray.expr()})"
    override fun roots() = listOf(ray)
}

fun createRayFromTexCoord(texCoord: Expression<vec2>) = object : Expression<ray>() {
    override fun expr() = "createRayFromTexCoord(${texCoord.expr()})"
    override fun roots() = listOf(texCoord)
}

fun createRaySphereHitRecord(ray: Expression<ray>, t: Expression<Float>, sphere: Expression<Sphere>) = object : Expression<HitRecord>() {
    override fun expr() = "createRaySphereHitRecord(${ray.expr()}, ${t.expr()}, ${sphere.expr()})"
    override fun roots() = listOf(ray, t, sphere)
}

fun hitRaySphere(ray: Expression<ray>, tMin: Expression<Float>, tMax: Expression<Float>, sphere: Expression<Sphere>) = object : Expression<HitRecord>() {
    override fun expr() = "hitRaySphere(${ray.expr()}, ${tMin.expr()}, ${tMax.expr()}, ${sphere.expr()})"
    override fun roots() = listOf(ray, tMin, tMax, sphere)
}

fun hitRayHitables(ray: Expression<ray>, tMin: Expression<Float>, tMax: Expression<Float>) = object : Expression<HitRecord>() {
    override fun expr() = "hitRayHitables(${ray.expr()}, ${tMin.expr()}, ${tMax.expr()})"
    override fun roots() = listOf(ray, tMin, tMax)
}

fun shadingRt(texCoord: Expression<vec2>) = object : Expression<vec4>() {
    override fun expr() = "shadingRt(${texCoord.expr()})"
    override fun roots() = listOf(texCoord)
}


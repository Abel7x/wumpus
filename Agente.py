from z3 import *
from cStringIO import StringIO

#Coordenadas en z3
x = Int('x')
y = Int('x')

#banderita para saber si tenemos el dinerito :3 
tenemosOro= False

#Coordenadas en python
x1=1
y1=1

B = Function('B', IntSort(), IntSort(), BoolSort()) #brisa
P = Function('P', IntSort(), IntSort(), BoolSort())	#pozo
H = Function('H', IntSort(), IntSort(), BoolSort()) #hedo
W = Function('W', IntSort(), IntSort(), BoolSort()) #wumpus
S = Function('S', IntSort(), IntSort(), BoolSort()) #safe
R = Function('R', IntSort(), IntSort(), BoolSort()) #resplandor
O = Function('O', IntSort(), IntSort(), BoolSort()) #oro

#Aqui definimos las funciones para aumentar las coordenadas cada que se avanza en base a la direccion a la cual
#apunta el monito
def este():
	global x1
	x1=x1+1
def sur():
	global y1
	if not y1==1:
		y1=y1-1
def oeste():
	global x1
	if not x1==1:
		x1=x1-1
def norte():
	global y1
	y1=y1+1

camino = {
	1 : este,
	2 : sur,
	3 : oeste,
	4 : norte,
    }

#direccion sirve para saber cual es la direccion del monito, en este caso comienza viendo al este la cual sera 1
# 1 = Este
# 2 = Sur
# 3 = Oeste
# 4 = Norte
direccion=1

def prueba(teoria):
	old_stdout = sys.stdout
	sys.stdout = mystdout = StringIO()
	prove(teoria)
	buffereses = mystdout.getvalue()
	lines = buffereses.rstrip().split('\n')
	sys.stdout = old_stdout
	if lines[0] == "counterexample":
		return False
	elif lines[0] == "proved":
		return True

#funcion para regresar
def regresar():
	global direccion
	sys.stdout.write("Derecha\n")
	sys.stdout.flush()
	if direccion < 4:
		direccion+=1
	else:
		direccion = 1
	sys.stdout.write("Derecha\n")
	sys.stdout.flush()
	if direccion < 4:
		direccion+=1
	else:
		direccion=1
	camino[direccion]()
	sys.stdout.write("Avanzar\n")
	sys.stdout.flush()
	sys.stdout.write("Izquierda\n")
	sys.stdout.flush()
	if direccion < 4:
		direccion=direccion+1
	else:
		direccion = 1

	camino[direccion]()
	sys.stdout.write("Avanzar\n")
	sys.stdout.flush()

#Reglas de inferencia
#Regla para cuando hay briza implica que para toda X,Y existe un pozo en los alrededores
pozo = ForAll( x, ForAll(y, Implies( B( x, y ), Or( P( x + 1, y ), P( x - 1, y ), P( x, y + 1 ), P( x, y - 1 ) ) ) ) )
no_pozo = ForAll( [x, y], Implies( Not(B( x, y )), And( Not(P( x + 1, y )), Not(P( x - 1, y )), Not(P( x, y + 1 )), Not(P( x, y - 1 ) ) ) ) )

#Regla para cuando hay hedor implica que para toda X,Y existe wumpus en los alrededores
hedor = ForAll( x, ForAll( y, Implies( H( x, y ), Or( W( x + 1, y ), W( x - 1, y ), W( x, y + 1 ), W( x, y - 1 ) ) ) ))
no_hedor = ForAll( [x, y], Implies( Not(H( x, y )), And( Not(W( x + 1, y )), Not(W( x - 1, y )), Not(W( x, y + 1 )), Not(W( x, y - 1 ) ) ) ) )

#Regla que establece lo que es una casilla, para todo X,Y que no tenga ni brisa ni hedor sera denominaca "Casilla Segura"
casilla_segura = ForAll([x, y], Implies( And( Not( B( x, y ) ), Not( H( x, y ) ), Not( P( x, y ) ), Not( W( x, y ) ) ), S( x, y ) ) )

#Regla que define si hay oro, para todo X,Y que tenga Resplandor implica que hay oro
casilla_con_oro = ForAll([x, y], Implies(R(x, y), O(x, y) ) )

#Fin de reglas de inferencia
#juntamos todas las reglas
reglas = And(pozo, hedor, no_pozo, no_hedor)

#Anadimos la percepcion inicial
persepcionInit = And( Not(B( 1, 1 )) , Not( H( 1, 1 ) ), Not( P( 1, 1 ) ), Not( W( 1, 1 ) ) )

reglas = And( reglas, persepcionInit )

#Leemos la linea de iniciar episodio e iniciar simulacion
sys.stdin.readline()
sys.stdin.readline()

while True:
	#La percepcion viene en un formato: "percepcion(hedor(no),brisa(no),resplandor(no),golpe(no),grito(no))"
	#per="percepcion(hedor(no),brisa(no),resplandor(no),golpe(no),grito(no))"
	per=sys.stdin.readline()
	done="EPISODE_ENDED" in per
	if done:
		sys.stdout.write("START SIMULATION REQUEST\n")
		sys.stdout.flush()
		sys.stdin.readline()
		sys.stdin.readline()
		pass
		#break
	#obtenemos las percepciones de la cadena que nos regresa
	hedor = "hedor(si)" in per
	brisa = "brisa(si)" in per
	resplandor = "resplandor(si)" in per
	golpe = "golpe(si)" in per
	grito = "grito(si)" in per
	#recojemos el oro
	if resplandor:
		sys.stdout.write("Agarrar\n")
		sys.stdout.flush()
		tenemosOro=True
	#si tenemos el oro y estamos en la casilla inicial escalamos
	if tenemosOro and x==1 and y==1:
		sys.stdout.write("Escalar\n")
		sys.stdout.flush()
		break
	if golpe:
		regresar()
		pass

	if hedor:
		reglas=And(reglas,H(x,y))	
	else:
		reglas=And(reglas,Not(H(x,y)))
	if brisa:
		reglas=And(reglas,B(x,y))	
	else:
		reglas=And(reglas,Not(B(x,y)))
	if resplandor:
		reglas=And(reglas,R(x,y))	
	else:
		reglas=And(reglas,Not(R(x,y)))

	#Probamos que sea seguro avanzar ah la siguiente casilla 
	teoria = Implies(reglas, And(Not(P(x+1,y)), Not(W(x+1,y))))

	result = prueba(teoria)
	if result:
		sys.stdout.write("Avanzar\n")
		sys.stdout.flush()
		x1 += 1
	else:
		regresar()
	'''print x1
	print y1
	print result'''

print "R.I.P. Agente :( "


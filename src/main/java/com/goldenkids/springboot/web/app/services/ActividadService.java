package com.goldenkids.springboot.web.app.services;

import com.goldenkids.springboot.web.app.controllers.AlumnoController;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.goldenkids.springboot.web.app.models.Actividad;
import com.goldenkids.springboot.web.app.models.Alumno;
import com.goldenkids.springboot.web.app.models.Salita;
import com.goldenkids.springboot.web.app.models.TipoActividad;
import com.goldenkids.springboot.web.app.models.TipoCantidad;
import com.goldenkids.springboot.web.app.models.TipoPanial;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import org.slf4j.LoggerFactory;

@Service
public class ActividadService {

    org.slf4j.Logger log = LoggerFactory.getLogger(AlumnoController.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void crearActividadObservacion(String observacion, Integer dniAlumno) {
        Actividad actividad = new Actividad();
        Alumno alumno = em.find(Alumno.class, dniAlumno);
        actividad.setTipoActividad(TipoActividad.OBSERVACION);
        actividad.setObservacion(observacion);
        actividad.setAlumno(alumno);
        actividad.setInicio(new Date());
        actividad.setFin(new Date());
        em.persist(actividad);
    }

    @Transactional
    public void crearActividad(TipoActividad tipoActividad, Integer cantidadLeche, TipoCantidad tipoCantidad,
            TipoPanial tipoPanial, String observacion, int dni) throws Exception {

        Alumno alumno = em.find(Alumno.class, dni);
        Actividad actividad = new Actividad();

        switch (tipoActividad) {

            case ENTRADA:
                actividad.setTipoActividad(tipoActividad.ASISTENCIA);
                actividad.setInicio(new Date());
                actividad.setAlumno(alumno);
                em.persist(actividad);
                break;

            case SALIDA:
                Actividad salida = estaEnClase(alumno);
                salida.setFin(new Date());
                em.merge(salida);
                break;

            case DESPIERTO:
                Actividad siesta = estaDurmiendo(alumno);
                siesta.setFin(new Date());
                em.merge(siesta);
                break;

            case DORMIDO:
                actividad.setTipoActividad(tipoActividad.SUEÑO);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                em.persist(actividad);
                break;

            case DESAYUNO:
                actividad.setTipoActividad(tipoActividad.DESAYUNO);
                actividad.setCantidad(tipoCantidad);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                actividad.setFin(new Date());
                em.persist(actividad);
                break;

            case ALMUERZO:
                actividad.setTipoActividad(tipoActividad.ALMUERZO);
                actividad.setCantidad(tipoCantidad);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                actividad.setFin(new Date());
                em.persist(actividad);
                break;

            case MERIENDA:
                actividad.setTipoActividad(tipoActividad.MERIENDA);
                actividad.setCantidad(tipoCantidad);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                actividad.setFin(new Date());
                em.persist(actividad);
                break;

            case LECHE:
                actividad.setTipoActividad(tipoActividad.LECHE);
                actividad.setCantidadLeche(cantidadLeche);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                actividad.setFin(new Date());
                em.persist(actividad);
                break;

            case PANIAL:
                actividad.setTipoActividad(tipoActividad.PANIAL);
                actividad.setTipoPanial(tipoPanial);
                actividad.setAlumno(alumno);
                actividad.setInicio(new Date());
                actividad.setFin(new Date());
                em.persist(actividad);
                break;
        }

    }

    @SuppressWarnings("unchecked")
    public List<Alumno> buscarAlumnnosPorSala(Salita salita) {
        return em.createQuery("SELECT a FROM Alumno a WHERE a.salita = :salita")
                .setParameter("salita", salita).getResultList();

    }

    @SuppressWarnings("unchecked")
    public List<Actividad> buscarActividadesPorAlumno(Alumno alumno, Date fecha, Date diaPosterior) {

        log.info("La fecha pasada es: " + fecha);

        return em.createQuery("SELECT a FROM Actividad a WHERE (a.alumno = :alumno) AND (a.inicio >= :fecha) AND (a.inicio < :diaPosterior) ORDER BY a.inicio")
                .setParameter("fecha", fecha)
                .setParameter("diaPosterior", diaPosterior)
                .setParameter("alumno", alumno)
                .getResultList();

    }

    public Date fechaFormateadaParaJpql(Date fecha) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(fecha));
    }

    public Date diaPosteariorFormateadoParaJpql(Date fecha) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date diaPosterior = calendar.getTime();
        return fechaFormateadaParaJpql(diaPosterior);
    }

    public Actividad
            buscarActividad(String id) {
        return em.find(Actividad.class,
                 id);
    }

    @Transactional
    public void eliminarActividad(String id) {
        em.remove(buscarActividad(id));
    }

    public Actividad estaDurmiendo(Alumno alumno) throws ParseException {
        Date hoy = fechaFormateadaParaJpql(new Date());
        Date diaSiguiente = diaPosteariorFormateadoParaJpql(hoy);
        Actividad actividad;
        try {
            actividad = (Actividad) em.createQuery("SELECT a FROM Actividad a WHERE (a.alumno = :alumno) AND (a.inicio >= :fecha) AND (a.inicio < :diaPosterior) AND a.tipoActividad = 'SUEÑO' AND a.fin = null")
                    .setParameter("fecha", hoy)
                    .setParameter("diaPosterior", diaSiguiente)
                    .setParameter("alumno", alumno)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException nre) {
            log.error("No hay resultados para el query de Siesta" + nre.getMessage());
            actividad = null;
        } catch (NonUniqueResultException nure) {
            log.error("Hay mas de un resultado para el query de Siesta" + nure.getMessage());
            actividad = null;
        }

        return actividad;
    }

    public Actividad estaEnClase(Alumno alumno) throws ParseException {
        Date hoy = fechaFormateadaParaJpql(new Date());
        Date diaSiguiente = diaPosteariorFormateadoParaJpql(hoy);
        Actividad actividad;
        try {
            actividad = (Actividad) em.createQuery("SELECT a FROM Actividad a WHERE (a.alumno = :alumno) AND (a.inicio >= :fecha) AND (a.inicio < :diaPosterior) AND a.tipoActividad = 'ASISTENCIA' AND a.fin = null")
                    .setParameter("fecha", hoy)
                    .setParameter("diaPosterior", diaSiguiente)
                    .setParameter("alumno", alumno)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException nre) {
            log.error("No hay resultados para el query de Asistencia" + nre.getMessage());
            actividad = null;
        } catch (NonUniqueResultException nure) {
            log.error("Hay mas de un resultado para el query de Asistencia" + nure.getMessage());
            actividad = null;
        }

        return actividad;

    }
}

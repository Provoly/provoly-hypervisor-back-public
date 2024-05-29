<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
                       xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
                       xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
                       xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <NamedLayer>
        <Name>Equipement</Name>
        <UserStyle>
            <Title>Hypervoly equipment</Title>
            <FeatureTypeStyle>
                <Transformation>
                    <ogc:Function name="gs:PointStacker">
                        <ogc:Function name="parameter">
                            <ogc:Literal>data</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>cellSize</ogc:Literal>
                            <ogc:Literal>50</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputBBOX</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_bbox</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputWidth</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_width</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputHeight</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_height</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                    </ogc:Function>
                </Transformation>
                <Rule>
                    <Title>Cluster</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsGreaterThan>
                            <ogc:PropertyName>count</ogc:PropertyName>
                            <ogc:Literal>1</ogc:Literal>
                        </ogc:PropertyIsGreaterThan>
                    </ogc:Filter>
                    <TextSymbolizer>
                        <Label>
                            <ogc:PropertyName>count</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Ubuntu</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                        </Fill>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#183C6B</CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#FFFFFF</CssParameter>
                                    <CssParameter name="stroke-width">2</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>48</Size>
                        </Graphic>
                    </TextSymbolizer>
                </Rule>
                <Rule>
                    <Title>Equipement</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyName>count</ogc:PropertyName>
                            <ogc:Literal>1</ogc:Literal>
                        </ogc:PropertyIsLessThanOrEqualTo>
                    </ogc:Filter>
                    <PointSymbolizer>
                        <Graphic>
                            <ExternalGraphic>
                                <OnlineResource xlink:type="simple" xlink:href="icons/${family}.svg"/>
                                <Format>image/svg</Format>
                            </ExternalGraphic>
                            <Size>48</Size>
                        </Graphic>
                    </PointSymbolizer>
                </Rule>
                <Rule>
                    <Title>Pastille bleue pour service en cours</Title>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:PropertyName>consommation</ogc:PropertyName>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <TextSymbolizer>
                        <Label>
                            <ogc:Literal>&#160;</ogc:Literal>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Ubuntu</CssParameter>
                            <CssParameter name="font-size">12</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.0</AnchorPointX>
                                    <AnchorPointY>0.0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>-16.0</DisplacementX>
                                    <DisplacementY>16.0</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                        </Fill>
                        <Graphic>
                            <ExternalGraphic>
                                <OnlineResource xlink:type="simple" xlink:href="icons/service.svg"/>
                                <Format>image/svg</Format>
                            </ExternalGraphic>
                            <Size>18</Size>
                        </Graphic>
                        <VendorOption name="conflictResolution">false</VendorOption>
                    </TextSymbolizer>
                </Rule>
                <Rule>
                    <Title>Pastille rouge pour high</Title>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyIsLike>
                                <ogc:PropertyName>criticality</ogc:PropertyName>
                                <ogc:Literal>HIGH</ogc:Literal>
                            </ogc:PropertyIsLike>
                        </ogc:And>
                    </ogc:Filter>
                    <TextSymbolizer>
                        <Label>
                            <ogc:PropertyName>consommation</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Ubuntu</CssParameter>
                            <CssParameter name="font-size">8</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.0</AnchorPointX>
                                    <AnchorPointY>0.0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>8</DisplacementX>
                                    <DisplacementY>18</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                        </Fill>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#FF0E0E</CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#FFFFFF</CssParameter>
                                    <CssParameter name="stroke-width">1</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>16</Size>
                        </Graphic>
                        <VendorOption name="conflictResolution">false</VendorOption>
                    </TextSymbolizer>
                </Rule>
                <Rule>
                    <Title>Pastille orange pour medium</Title>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyIsLike>
                                <ogc:PropertyName>criticality</ogc:PropertyName>
                                <ogc:Literal>MEDIUM</ogc:Literal>
                            </ogc:PropertyIsLike>
                        </ogc:And>
                    </ogc:Filter>
                    <TextSymbolizer>
                        <Label>
                            <ogc:PropertyName>consommation</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Ubuntu</CssParameter>
                            <CssParameter name="font-size">8</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.0</AnchorPointX>
                                    <AnchorPointY>0.0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>8</DisplacementX>
                                    <DisplacementY>18</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                        </Fill>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#FD8902</CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#FFFFFF</CssParameter>
                                    <CssParameter name="stroke-width">1</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>16</Size>
                        </Graphic>
                        <VendorOption name="conflictResolution">false</VendorOption>
                    </TextSymbolizer>
                </Rule>
                <Rule>
                    <Title>Pastille jaune pour low</Title>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                            <ogc:PropertyIsLike>
                                <ogc:PropertyName>criticality</ogc:PropertyName>
                                <ogc:Literal>LOW</ogc:Literal>
                            </ogc:PropertyIsLike>
                        </ogc:And>
                    </ogc:Filter>
                    <TextSymbolizer>
                        <Label>
                            <ogc:PropertyName>consommation</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">Ubuntu</CssParameter>
                            <CssParameter name="font-size">8</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.0</AnchorPointX>
                                    <AnchorPointY>0.0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>8</DisplacementX>
                                    <DisplacementY>18</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                        </Fill>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#FCD858</CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#FFFFFF</CssParameter>
                                    <CssParameter name="stroke-width">1</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>16</Size>
                        </Graphic>
                        <VendorOption name="conflictResolution">false</VendorOption>
                    </TextSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>
